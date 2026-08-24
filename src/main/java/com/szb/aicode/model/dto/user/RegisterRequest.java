package com.szb.aicode.model.dto.user;


import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterRequest implements Serializable {

    private String username;
    private String password;
    private String check;

}
