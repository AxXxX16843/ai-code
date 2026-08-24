package com.szb.aicode.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}
