#!/usr/bin/env python3
"""
buffalo test receiver -- SPECS.md 1.1 video channel only, no OBS/Rust involved.

Listens for the phone's TCP connection, strips the 4-byte big-endian length prefix off each
frame, and writes the raw Annex-B H264 bytes to stdout. Pipe that straight into ffplay (which
can decode a raw Annex-B stream directly) for an actual visual test of the encode+network path
before the OBS plugin is anywhere near working.

Usage:
    python3 test_receiver.py [port]              # defaults to 5757, matching PreviewFragment
    python3 test_receiver.py | ffplay -f h264 -i -

Point the phone app at this machine's IP on that port and press record.
"""
import socket
import struct
import sys


def recvall(sock: socket.socket, n: int) -> bytes | None:
    buf = b""
    while len(buf) < n:
        chunk = sock.recv(n - len(buf))
        if not chunk:
            return None
        buf += chunk
    return buf


def main() -> None:
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 5757

    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(("0.0.0.0", port))
    srv.listen(1)
    print(f"[test_receiver] listening on :{port}, waiting for the phone...", file=sys.stderr)

    conn, addr = srv.accept()
    print(f"[test_receiver] connected: {addr}", file=sys.stderr)

    frame_count = 0
    try:
        while True:
            header = recvall(conn, 4)
            if header is None:
                break
            (length,) = struct.unpack(">I", header)

            if length > 32 * 1024 * 1024:
                print(f"[test_receiver] implausible frame length {length}, "
                      f"the stream is probably desynced -- stopping", file=sys.stderr)
                break

            payload = recvall(conn, length)
            if payload is None:
                break

            sys.stdout.buffer.write(payload)
            sys.stdout.buffer.flush()

            frame_count += 1
            if frame_count % 30 == 0:
                print(f"[test_receiver] {frame_count} frames received", file=sys.stderr)
    finally:
        conn.close()
        print(f"[test_receiver] connection closed after {frame_count} frames", file=sys.stderr)


if __name__ == "__main__":
    main()
