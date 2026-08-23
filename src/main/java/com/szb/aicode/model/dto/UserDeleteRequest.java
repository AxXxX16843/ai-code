package com.szb.aicode.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;


}
