package com.github.adamantcheese.chan.ui.settings;

import android.view.View;

import com.github.adamantcheese.chan.ui.controller.settings.SettingsController;

public class TextSettingView
        extends SettingView {
    public TextSettingView(SettingsController settingsController, String name) {
        super(settingsController, name);
    }

    @Override
    public void setView(View view) {
        super.setView(view);
    }

    @Override
    public String getBottomDescription() {
        return "";
    }
}