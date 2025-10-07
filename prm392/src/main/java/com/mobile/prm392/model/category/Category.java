package com.mobile.prm392.model.category;

import com.mobile.prm392.entities.Podcast;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Long id;
    private String name;
    private List<Podcast> podcasts;
}
