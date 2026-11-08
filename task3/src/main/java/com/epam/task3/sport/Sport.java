package com.epam.task3.sport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("sport")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sport {

    @Id
    private Integer id;

    @Column("name")
    private String name;
}
