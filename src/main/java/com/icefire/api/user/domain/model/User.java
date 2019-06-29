package com.icefire.api.user.domain.model;

import com.icefire.api.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Lob
    private byte[] publicKey;

}
