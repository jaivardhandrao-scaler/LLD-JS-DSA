package com.assignment.question;

import lombok.Getter;

@IntrinsicState
@Getter
public class GraphicIntrinsicState {
    private Image image;
    private int width;
    private int height;
    private String color;

    private GraphicType type;
}