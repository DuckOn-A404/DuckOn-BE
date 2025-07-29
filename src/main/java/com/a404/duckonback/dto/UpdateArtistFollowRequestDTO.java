package com.a404.duckonback.dto;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@Getter @Setter
public class UpdateArtistFollowRequestDTO {
    @NotNull
    private List<Long> artistList;
}
