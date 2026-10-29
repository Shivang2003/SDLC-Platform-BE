package com.sdlc.sdlc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldChange {
    private Object oldValue;
    private Object newValue;
}