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

public class PreviewStabilizationFragmentDirections {
  private PreviewStabilizationFragmentDirections() {
  }

  @NonNull
  public static ActionPreviewStabilizationToEncodeApi actionPreviewStabilizationToEncodeApi(
      @NonNull String cameraId, int width, int height, int fps, long dynamicRange, int colorSpace,
      boolean previewStabilization) {
    return new ActionPreviewStabilizationToEncodeApi(cameraId, width, height, fps, dynamicRange, colorSpace, previewStabilization);
  }

  public static class ActionPreviewStabilizationToEncodeApi implements NavDirections {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    private ActionPreviewStabilizationToEncodeApi(@NonNull String cameraId, int width, int height,
        int fps, long dynamicRange, int colorSpace, boolean previewStabilization) {
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
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionPreviewStabilizationToEncodeApi setCameraId(@NonNull String cameraId) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionPreviewStabilizationToEncodeApi setWidth(int width) {
      this.arguments.put("width", width);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionPreviewStabilizationToEncodeApi setHeight(int height) {
      this.arguments.put("height", height);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionPreviewStabilizationToEncodeApi setFps(int fps) {
      this.arguments.put("fps", fps);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionPreviewStabilizationToEncodeApi setDynamicRange(long dynamicRange) {
      this.arguments.put("dynamicRange", dynamicRange);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionPreviewStabilizationToEncodeApi setColorSpace(int colorSpace) {
      this.arguments.put("colorSpace", colorSpace);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionPreviewStabilizationToEncodeApi setPreviewStabilization(
        boolean previewStabilization) {
      this.arguments.put("previewStabilization", previewStabilization);
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
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_preview_stabilization_to_encode_api;
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

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionPreviewStabilizationToEncodeApi that = (ActionPreviewStabilizationToEncodeApi) object;
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
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionPreviewStabilizationToEncodeApi(actionId=" + getActionId() + "){"
          + "cameraId=" + getCameraId()
          + ", width=" + getWidth()
          + ", height=" + getHeight()
          + ", fps=" + getFps()
          + ", dynamicRange=" + getDynamicRange()
          + ", colorSpace=" + getColorSpace()
          + ", previewStabilization=" + getPreviewStabilization()
          + "}";
    }
  }
}
