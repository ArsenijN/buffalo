package com.arseniusgen.buffalo.fragments;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.arseniusgen.buffalo.R;

public class PreviewFragmentDirections {
  private PreviewFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionPreviewToPermissions() {
    return new ActionOnlyNavDirections(R.id.action_preview_to_permissions);
  }
}
