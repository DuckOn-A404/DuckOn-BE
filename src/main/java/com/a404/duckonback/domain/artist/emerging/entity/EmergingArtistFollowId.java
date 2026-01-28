package com.a404.duckonback.domain.artist.emerging.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EmergingArtistFollowId implements Serializable {
    private Long user; // User.id
    private Long emergingArtist; // EmergingArtist.emergingArtistId
}
