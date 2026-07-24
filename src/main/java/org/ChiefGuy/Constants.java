package org.ChiefGuy;

public enum Constants {
    ParagraphSize(50),
    DefaultFontSize(12),
    DefaultFirstLineIndent(0),
    DefaultMarginTop(0),
    DefaultMarginBottom(0);
    final public int value;
    Constants(int value) {
        this.value = value;
    }
}
