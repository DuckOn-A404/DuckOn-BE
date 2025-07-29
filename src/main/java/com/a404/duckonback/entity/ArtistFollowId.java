package com.a404.duckonback.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ArtistFollowId implements Serializable {
    private Long user;       // User.id
    private Long artist;    // Artist.artistId
}
