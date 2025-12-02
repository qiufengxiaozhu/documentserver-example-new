package com.filez.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Watermark configuration
 * Example:
 * line1: "Li Ming liming@lenovo.com"; // e.g., file last updater information
 * line2: "Zhang San zhangshan@lenovo.com"; // e.g., file editor/viewer information
 * line3: ""; // e.g., other custom text
 * line4: ""; // e.g., other custom text
 * withDate: true; // Whether to include date after each line of text
 * fontcolor: "#FD4147"; // RGB value
 * transparent: 30; // 0 - 100, transparency. Smaller values mean more transparent effect
 * rotation: 315; // Rotation angle 0 - 360, left tilt=315, right tilt=45, horizontal=0
 * fontsize: “72”;
 * font: "SimHei";
 * spacing: 50; // Line spacing and column spacing are both 50 pixels
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocWaterMark {

    @Builder.Default
    private String line1 = "";

    private String line2;

    private String line3;

    private String line4;

    private boolean withDate;

    private String fontcolor;

    private int transparent;

    private int spacing;

    private int rotation;

    private String fontsize;

    @Builder.Default
    private String font = "SimHei";
}
