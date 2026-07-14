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

public class SelectorFragmentDirections {
  private SelectorFragmentDirections() {
  }

  @NonNull
  public static ActionSelectorToDynamicRange actionSelectorToDynamicRange(@NonNull String cameraId,
      int width, int height, int fps) {
    return new ActionSelectorToDynamicRange(cameraId, width, height, fps);
  }

  @NonNull
  public static ActionSelectorToColorSpace actionSelectorToColorSpace(@NonNull String cameraId,
      int width, int height, int fps, long dynamicRange) {
    return new ActionSelectorToColorSpace(cameraId, width, height, fps, dynamicRange);
  }

  @NonNull
  public static ActionSelectorToPreviewStabilization actionSelectorToPreviewStabilization(
      @NonNull String cameraId, int width, int height, int fps, long dynamicRange, int colorSpace) {
    return new ActionSelectorToPreviewStabilization(cameraId, width, height, fps, dynamicRange, colorSpace);
  }

  @NonNull
  public static ActionSelectorToEncodeApi actionSelectorToEncodeApi(@NonNull String cameraId,
      int width, int height, int fps, long dynamicRange, int colorSpace,
      boolean previewStabilization) {
    return new ActionSelectorToEncodeApi(cameraId, width, height, fps, dynamicRange, colorSpace, previewStabilization);
  }

  public static class ActionSelectorToDynamicRange implements NavDirections {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    private ActionSelectorToDynamicRange(@NonNull String cameraId, int width, int height, int fps) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      this.arguments.put("width", width);
      this.arguments.put("height", height);
      this.arguments.put("fps", fps);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToDynamicRange setCameraId(@NonNull String cameraId) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToDynamicRange setWidth(int width) {
      this.arguments.put("width", width);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToDynamicRange setHeight(int height) {
      this.arguments.put("height", height);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToDynamicRange setFps(int fps) {
      this.arguments.put("fps", fps);
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
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_selector_to_dynamic_range;
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

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionSelectorToDynamicRange that = (ActionSelectorToDynamicRange) object;
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
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionSelectorToDynamicRange(actionId=" + getActionId() + "){"
          + "cameraId=" + getCameraId()
          + ", width=" + getWidth()
          + ", height=" + getHeight()
          + ", fps=" + getFps()
          + "}";
    }
  }

  public static class ActionSelectorToColorSpace implements NavDirections {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    private ActionSelectorToColorSpace(@NonNull String cameraId, int width, int height, int fps,
        long dynamicRange) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      this.arguments.put("width", width);
      this.arguments.put("height", height);
      this.arguments.put("fps", fps);
      this.arguments.put("dynamicRange", dynamicRange);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToColorSpace setCameraId(@NonNull String cameraId) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToColorSpace setWidth(int width) {
      this.arguments.put("width", width);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToColorSpace setHeight(int height) {
      this.arguments.put("height", height);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToColorSpace setFps(int fps) {
      this.arguments.put("fps", fps);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToColorSpace setDynamicRange(long dynamicRange) {
      this.arguments.put("dynamicRange", dynamicRange);
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
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_selector_to_color_space;
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

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionSelectorToColorSpace that = (ActionSelectorToColorSpace) object;
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
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionSelectorToColorSpace(actionId=" + getActionId() + "){"
          + "cameraId=" + getCameraId()
          + ", width=" + getWidth()
          + ", height=" + getHeight()
          + ", fps=" + getFps()
          + ", dynamicRange=" + getDynamicRange()
          + "}";
    }
  }

  public static class ActionSelectorToPreviewStabilization implements NavDirections {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    private ActionSelectorToPreviewStabilization(@NonNull String cameraId, int width, int height,
        int fps, long dynamicRange, int colorSpace) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      this.arguments.put("width", width);
      this.arguments.put("height", height);
      this.arguments.put("fps", fps);
      this.arguments.put("dynamicRange", dynamicRange);
      this.arguments.put("colorSpace", colorSpace);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToPreviewStabilization setCameraId(@NonNull String cameraId) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToPreviewStabilization setWidth(int width) {
      this.arguments.put("width", width);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToPreviewStabilization setHeight(int height) {
      this.arguments.put("height", height);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToPreviewStabilization setFps(int fps) {
      this.arguments.put("fps", fps);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToPreviewStabilization setDynamicRange(long dynamicRange) {
      this.arguments.put("dynamicRange", dynamicRange);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToPreviewStabilization setColorSpace(int colorSpace) {
      this.arguments.put("colorSpace", colorSpace);
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
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_selector_to_preview_stabilization;
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

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionSelectorToPreviewStabilization that = (ActionSelectorToPreviewStabilization) object;
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
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionSelectorToPreviewStabilization(actionId=" + getActionId() + "){"
          + "cameraId=" + getCameraId()
          + ", width=" + getWidth()
          + ", height=" + getHeight()
          + ", fps=" + getFps()
          + ", dynamicRange=" + getDynamicRange()
          + ", colorSpace=" + getColorSpace()
          + "}";
    }
  }

  public static class ActionSelectorToEncodeApi implements NavDirections {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    private ActionSelectorToEncodeApi(@NonNull String cameraId, int width, int height, int fps,
        long dynamicRange, int colorSpace, boolean previewStabilization) {
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
    public ActionSelectorToEncodeApi setCameraId(@NonNull String cameraId) {
      if (cameraId == null) {
        throw new IllegalArgumentException("Argument \"camera_id\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("camera_id", cameraId);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToEncodeApi setWidth(int width) {
      this.arguments.put("width", width);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToEncodeApi setHeight(int height) {
      this.arguments.put("height", height);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToEncodeApi setFps(int fps) {
      this.arguments.put("fps", fps);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToEncodeApi setDynamicRange(long dynamicRange) {
      this.arguments.put("dynamicRange", dynamicRange);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToEncodeApi setColorSpace(int colorSpace) {
      this.arguments.put("colorSpace", colorSpace);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionSelectorToEncodeApi setPreviewStabilization(boolean previewStabilization) {
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
      return R.id.action_selector_to_encode_api;
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
      ActionSelectorToEncodeApi that = (ActionSelectorToEncodeApi) object;
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
      return "ActionSelectorToEncodeApi(actionId=" + getActionId() + "){"
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
