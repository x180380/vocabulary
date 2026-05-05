package com.vocabapp.presentation.common;

import android.content.Context;

import com.vocabapp.R;

public class ColorUtils {

    private static final int[] CARD_COLOR_RES = {
        R.color.card_color_0,
        R.color.card_color_1,
        R.color.card_color_2,
        R.color.card_color_3,
        R.color.card_color_4,
        R.color.card_color_5,
        R.color.card_color_6,
        R.color.card_color_7
    };

    public static int getCardColor(Context context, int colorIndex) {
        int safeIndex = Math.abs(colorIndex) % CARD_COLOR_RES.length;
        return context.getColor(CARD_COLOR_RES[safeIndex]);
    }
}
