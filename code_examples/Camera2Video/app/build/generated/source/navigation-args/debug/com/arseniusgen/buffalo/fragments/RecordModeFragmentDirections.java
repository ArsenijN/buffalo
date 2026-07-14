package com.arseniusgen.buffalo.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import com.arseniusgen.buffalo.R;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class RecordModeFragmentDirections {
  private RecordModeFragmentDirections() {
  }

  @NonNull
  public static ActionRecordModeToPreview actionRecordModeToPreview(@NonNull String cameraId,
      int width, int height, int fps, long dynamicRange, int colorSpace,
      boolean previewStabilization, boolean useMediaRecorder, int videoCodec, boolean filterOn,
      boolean useHardware, int transfer) {
    return new ActionRecordModeToPreview(cameraId, width, height, fps, dynamicRange, colorSpace, previewStabilization, useMediaRecorder, videoCodec, filterOn, useHardware, transfer);
  }

  @NonNull
  public static ActionRecordModeToFilter actionRecordModeToFilter(@NonNull String cameraId,
      int width, int height, int fps, long dynamicRange, int colorSpace,
      boolean previewStabilization, boolean useMediaRecorder, int videoCodec) {
    return new ActionRecordModeToFilter(cameraId, width, height, fps, dynamicRange, colorSpace, previewStabilization, useMediaRecorder, videoCodec);
  }

  public static class ActionRecordModeToPreview implements NavDirections {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    private ActionRecordModeToPreview(@NonNull String cameraId, int width, int height, int fps,
        long dynamicRange, int colorSpace, boolean previewStabilization, boolean useMediaRecorder,
        int videoCodec, boolean filterOn, boolean useHardware, int transfer) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      this.arguments.put("width", width);
      this.arguments.put("height", height);
      this.arguments.put("fps", fps);
      this.arguments.put("dynamicRange", dynamicRange);
      this.arguments.put("colorSpace", colorSpace);
      this.arguments.put("previewStabilization", previewStabilization);
      this.arguments.put("useMediaRecorder", useMediaRecorder);
      this.arguments.put("videoCodec", videoCodec);
      this.arguments.put("filterOn", filterOn);
      this.arguments.put("useHardware", useHardware);
      this.arguments.put("transfer", transfer);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setCameraId(@NonNull String cameraId) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setWidth(int width) {
      this.arguments.put("width", width);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setHeight(int height) {
      this.arguments.put("height", height);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setFps(int fps) {
      this.arguments.put("fps", fps);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setDynamicRange(long dynamicRange) {
      this.arguments.put("dynamicRange", dynamicRange);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setColorSpace(int colorSpace) {
      this.arguments.put("colorSpace", colorSpace);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setPreviewStabilization(boolean previewStabilization) {
      this.arguments.put("previewStabilization", previewStabilization);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setUseMediaRecorder(boolean useMediaRecorder) {
      this.arguments.put("useMediaRecorder", useMediaRecorder);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setVideoCodec(int videoCodec) {
      this.arguments.put("videoCodec", videoCodec);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setFilterOn(boolean filterOn) {
      this.arguments.put("filterOn", filterOn);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setUseHardware(boolean useHardware) {
      this.arguments.put("useHardware", useHardware);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToPreview setTransfer(int transfer) {
      this.arguments.put("transfer", transfer);
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public Bundle getArguments() {
      Bundle __result = new Bundle();
      if (arguments.containsKey("camera_id")) {
        String cameraId = (String) arguments.get("camera_id");
        __result.putString("camera_id", cameraId);
      }
      if (arguments.containsKey("width")) {
        int width = (int) arguments.get("width");
        __result.putInt("width", width);
      }
      if (arguments.containsKey("height")) {
        int height = (int) arguments.get("height");
        __result.putInt("height", height);
      }
      if (arguments.containsKey("fps")) {
        int fps = (int) arguments.get("fps");
        __result.putInt("fps", fps);
      }
      if (arguments.containsKey("dynamicRange")) {
        long dynamicRange = (long) arguments.get("dynamicRange");
        __result.putLong("dynamicRange", dynamicRange);
      }
      if (arguments.containsKey("colorSpace")) {
        int colorSpace = (int) arguments.get("colorSpace");
        __result.putInt("colorSpace", colorSpace);
      }
      if (arguments.containsKey("previewStabilization")) {
        boolean previewStabilization = (boolean) arguments.get("previewStabilization");
        __result.putBoolean("previewStabilization", previewStabilization);
      }
      if (arguments.containsKey("useMediaRecorder")) {
        boolean useMediaRecorder = (boolean) arguments.get("useMediaRecorder");
        __result.putBoolean("useMediaRecorder", useMediaRecorder);
      }
      if (arguments.containsKey("videoCodec")) {
        int videoCodec = (int) arguments.get("videoCodec");
        __result.putInt("videoCodec", videoCodec);
      }
      if (arguments.containsKey("filterOn")) {
        boolean filterOn = (boolean) arguments.get("filterOn");
        __result.putBoolean("filterOn", filterOn);
      }
      if (arguments.containsKey("useHardware")) {
        boolean useHardware = (boolean) arguments.get("useHardware");
        __result.putBoolean("useHardware", useHardware);
      }
      if (arguments.containsKey("transfer")) {
        int transfer = (int) arguments.get("transfer");
        __result.putInt("transfer", transfer);
      }
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_record_mode_to_preview;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public String getCameraId() {
      return (String) arguments.get("camera_id");
    }

    @SuppressWarnings("unchecked")
    public int getWidth() {
      return (int) arguments.get("width");
    }

    @SuppressWarnings("unchecked")
    public int getHeight() {
      return (int) arguments.get("height");
    }

    @SuppressWarnings("unchecked")
    public int getFps() {
      return (int) arguments.get("fps");
    }

    @SuppressWarnings("unchecked")
    public long getDynamicRange() {
      return (long) arguments.get("dynamicRange");
    }

    @SuppressWarnings("unchecked")
    public int getColorSpace() {
      return (int) arguments.get("colorSpace");
    }

    @SuppressWarnings("unchecked")
    public boolean getPreviewStabilization() {
      return (boolean) arguments.get("previewStabilization");
    }

    @SuppressWarnings("unchecked")
    public boolean getUseMediaRecorder() {
      return (boolean) arguments.get("useMediaRecorder");
    }

    @SuppressWarnings("unchecked")
    public int getVideoCodec() {
      return (int) arguments.get("videoCodec");
    }

    @SuppressWarnings("unchecked")
    public boolean getFilterOn() {
      return (boolean) arguments.get("filterOn");
    }

    @SuppressWarnings("unchecked")
    public boolean getUseHardware() {
      return (boolean) arguments.get("useHardware");
    }

    @SuppressWarnings("unchecked")
    public int getTransfer() {
      return (int) arguments.get("transfer");
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionRecordModeToPreview that = (ActionRecordModeToPreview) object;
      if (arguments.containsKey("camera_id") != that.arguments.containsKey("camera_id")) {
        return false;
      }
      if (getCameraId() != null ? !getCameraId().equals(that.getCameraId()) : that.getCameraId() != null) {
        return false;
      }
      if (arguments.containsKey("width") != that.arguments.containsKey("width")) {
        return false;
      }
      if (getWidth() != that.getWidth()) {
        return false;
      }
      if (arguments.containsKey("height") != that.arguments.containsKey("height")) {
        return false;
      }
      if (getHeight() != that.getHeight()) {
        return false;
      }
      if (arguments.containsKey("fps") != that.arguments.containsKey("fps")) {
        return false;
      }
      if (getFps() != that.getFps()) {
        return false;
      }
      if (arguments.containsKey("dynamicRange") != that.arguments.containsKey("dynamicRange")) {
        return false;
      }
      if (getDynamicRange() != that.getDynamicRange()) {
        return false;
      }
      if (arguments.containsKey("colorSpace") != that.arguments.containsKey("colorSpace")) {
        return false;
      }
      if (getColorSpace() != that.getColorSpace()) {
        return false;
      }
      if (arguments.containsKey("previewStabilization") != that.arguments.containsKey("previewStabilization")) {
        return false;
      }
      if (getPreviewStabilization() != that.getPreviewStabilization()) {
        return false;
      }
      if (arguments.containsKey("useMediaRecorder") != that.arguments.containsKey("useMediaRecorder")) {
        return false;
      }
      if (getUseMediaRecorder() != that.getUseMediaRecorder()) {
        return false;
      }
      if (arguments.containsKey("videoCodec") != that.arguments.containsKey("videoCodec")) {
        return false;
      }
      if (getVideoCodec() != that.getVideoCodec()) {
        return false;
      }
      if (arguments.containsKey("filterOn") != that.arguments.containsKey("filterOn")) {
        return false;
      }
      if (getFilterOn() != that.getFilterOn()) {
        return false;
      }
      if (arguments.containsKey("useHardware") != that.arguments.containsKey("useHardware")) {
        return false;
      }
      if (getUseHardware() != that.getUseHardware()) {
        return false;
      }
      if (arguments.containsKey("transfer") != that.arguments.containsKey("transfer")) {
        return false;
      }
      if (getTransfer() != that.getTransfer()) {
        return false;
      }
      if (getActionId() != that.getActionId()) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + (getCameraId() != null ? getCameraId().hashCode() : 0);
      result = 31 * result + getWidth();
      result = 31 * result + getHeight();
      result = 31 * result + getFps();
      result = 31 * result + (int)(getDynamicRange() ^ (getDynamicRange() >>> 32));
      result = 31 * result + getColorSpace();
      result = 31 * result + (getPreviewStabilization() ? 1 : 0);
      result = 31 * result + (getUseMediaRecorder() ? 1 : 0);
      result = 31 * result + getVideoCodec();
      result = 31 * result + (getFilterOn() ? 1 : 0);
      result = 31 * result + (getUseHardware() ? 1 : 0);
      result = 31 * result + getTransfer();
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionRecordModeToPreview(actionId=" + getActionId() + "){"
          + "cameraId=" + getCameraId()
          + ", width=" + getWidth()
          + ", height=" + getHeight()
          + ", fps=" + getFps()
          + ", dynamicRange=" + getDynamicRange()
          + ", colorSpace=" + getColorSpace()
          + ", previewStabilization=" + getPreviewStabilization()
          + ", useMediaRecorder=" + getUseMediaRecorder()
          + ", videoCodec=" + getVideoCodec()
          + ", filterOn=" + getFilterOn()
          + ", useHardware=" + getUseHardware()
          + ", transfer=" + getTransfer()
          + "}";
    }
  }

  public static class ActionRecordModeToFilter implements NavDirections {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    private ActionRecordModeToFilter(@NonNull String cameraId, int width, int height, int fps,
        long dynamicRange, int colorSpace, boolean previewStabilization, boolean useMediaRecorder,
        int videoCodec) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      this.arguments.put("width", width);
      this.arguments.put("height", height);
      this.arguments.put("fps", fps);
      this.arguments.put("dynamicRange", dynamicRange);
      this.arguments.put("colorSpace", colorSpace);
      this.arguments.put("previewStabilization", previewStabilization);
      this.arguments.put("useMediaRecorder", useMediaRecorder);
      this.arguments.put("videoCodec", videoCodec);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setCameraId(@NonNull String cameraId) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setWidth(int width) {
      this.arguments.put("width", width);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setHeight(int height) {
      this.arguments.put("height", height);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setFps(int fps) {
      this.arguments.put("fps", fps);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setDynamicRange(long dynamicRange) {
      this.arguments.put("dynamicRange", dynamicRange);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setColorSpace(int colorSpace) {
      this.arguments.put("colorSpace", colorSpace);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setPreviewStabilization(boolean previewStabilization) {
      this.arguments.put("previewStabilization", previewStabilization);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setUseMediaRecorder(boolean useMediaRecorder) {
      this.arguments.put("useMediaRecorder", useMediaRecorder);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionRecordModeToFilter setVideoCodec(int videoCodec) {
      this.arguments.put("videoCodec", videoCodec);
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public Bundle getArguments() {
      Bundle __result = new Bundle();
      if (arguments.containsKey("camera_id")) {
        String cameraId = (String) arguments.get("camera_id");
        __result.putString("camera_id", cameraId);
      }
      if (arguments.containsKey("width")) {
        int width = (int) arguments.get("width");
        __result.putInt("width", width);
      }
      if (arguments.containsKey("height")) {
        int height = (int) arguments.get("height");
        __result.putInt("height", height);
      }
      if (arguments.containsKey("fps")) {
        int fps = (int) arguments.get("fps");
        __result.putInt("fps", fps);
      }
      if (arguments.containsKey("dynamicRange")) {
        long dynamicRange = (long) arguments.get("dynamicRange");
        __result.putLong("dynamicRange", dynamicRange);
      }
      if (arguments.containsKey("colorSpace")) {
        int colorSpace = (int) arguments.get("colorSpace");
        __result.putInt("colorSpace", colorSpace);
      }
      if (arguments.containsKey("previewStabilization")) {
        boolean previewStabilization = (boolean) arguments.get("previewStabilization");
        __result.putBoolean("previewStabilization", previewStabilization);
      }
      if (arguments.containsKey("useMediaRecorder")) {
        boolean useMediaRecorder = (boolean) arguments.get("useMediaRecorder");
        __result.putBoolean("useMediaRecorder", useMediaRecorder);
      }
      if (arguments.containsKey("videoCodec")) {
        int videoCodec = (int) arguments.get("videoCodec");
        __result.putInt("videoCodec", videoCodec);
      }
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_record_mode_to_filter;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public String getCameraId() {
      return (String) arguments.get("camera_id");
    }

    @SuppressWarnings("unchecked")
    public int getWidth() {
      return (int) arguments.get("width");
    }

    @SuppressWarnings("unchecked")
    public int getHeight() {
      return (int) arguments.get("height");
    }

    @SuppressWarnings("unchecked")
    public int getFps() {
      return (int) arguments.get("fps");
    }

    @SuppressWarnings("unchecked")
    public long getDynamicRange() {
      return (long) arguments.get("dynamicRange");
    }

    @SuppressWarnings("unchecked")
    public int getColorSpace() {
      return (int) arguments.get("colorSpace");
    }

    @SuppressWarnings("unchecked")
    public boolean getPreviewStabilization() {
      return (boolean) arguments.get("previewStabilization");
    }

    @SuppressWarnings("unchecked")
    public boolean getUseMediaRecorder() {
      return (boolean) arguments.get("useMediaRecorder");
    }

    @SuppressWarnings("unchecked")
    public int getVideoCodec() {
      return (int) arguments.get("videoCodec");
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionRecordModeToFilter that = (ActionRecordModeToFilter) object;
      if (arguments.containsKey("camera_id") != that.arguments.containsKey("camera_id")) {
        return false;
      }
      if (getCameraId() != null ? !getCameraId().equals(that.getCameraId()) : that.getCameraId() != null) {
        return false;
      }
      if (arguments.containsKey("width") != that.arguments.containsKey("width")) {
        return false;
      }
      if (getWidth() != that.getWidth()) {
        return false;
      }
      if (arguments.containsKey("height") != that.arguments.containsKey("height")) {
        return false;
      }
      if (getHeight() != that.getHeight()) {
        return false;
      }
      if (arguments.containsKey("fps") != that.arguments.containsKey("fps")) {
        return false;
      }
      if (getFps() != that.getFps()) {
        return false;
      }
      if (arguments.containsKey("dynamicRange") != that.arguments.containsKey("dynamicRange")) {
        return false;
      }
      if (getDynamicRange() != that.getDynamicRange()) {
        return false;
      }
      if (arguments.containsKey("colorSpace") != that.arguments.containsKey("colorSpace")) {
        return false;
      }
      if (getColorSpace() != that.getColorSpace()) {
        return false;
      }
      if (arguments.containsKey("previewStabilization") != that.arguments.containsKey("previewStabilization")) {
        return false;
      }
      if (getPreviewStabilization() != that.getPreviewStabilization()) {
        return false;
      }
      if (arguments.containsKey("useMediaRecorder") != that.arguments.containsKey("useMediaRecorder")) {
        return false;
      }
      if (getUseMediaRecorder() != that.getUseMediaRecorder()) {
        return false;
      }
      if (arguments.containsKey("videoCodec") != that.arguments.containsKey("videoCodec")) {
        return false;
      }
      if (getVideoCodec() != that.getVideoCodec()) {
        return false;
      }
      if (getActionId() != that.getActionId()) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + (getCameraId() != null ? getCameraId().hashCode() : 0);
      result = 31 * result + getWidth();
      result = 31 * result + getHeight();
      result = 31 * result + getFps();
      result = 31 * result + (int)(getDynamicRange() ^ (getDynamicRange() >>> 32));
      result = 31 * result + getColorSpace();
      result = 31 * result + (getPreviewStabilization() ? 1 : 0);
      result = 31 * result + (getUseMediaRecorder() ? 1 : 0);
      result = 31 * result + getVideoCodec();
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionRecordModeToFilter(actionId=" + getActionId() + "){"
          + "cameraId=" + getCameraId()
          + ", width=" + getWidth()
          + ", height=" + getHeight()
          + ", fps=" + getFps()
          + ", dynamicRange=" + getDynamicRange()
          + ", colorSpace=" + getColorSpace()
          + ", previewStabilization=" + getPreviewStabilization()
          + ", useMediaRecorder=" + getUseMediaRecorder()
          + ", videoCodec=" + getVideoCodec()
          + "}";
    }
  }
}
