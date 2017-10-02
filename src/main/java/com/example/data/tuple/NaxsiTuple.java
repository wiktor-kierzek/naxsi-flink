package com.example.data.tuple;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * Created by wiktor.kierzek on 02.10.2017.
 */
@AllArgsConstructor
@Getter
public class NaxsiTuple implements Serializable {
    private String log;
    private String message;
}