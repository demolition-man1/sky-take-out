package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDishVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long dishId;

    private String name;

    private BigDecimal price;

    private Integer copies;

    private String image;

    private String description;
}
