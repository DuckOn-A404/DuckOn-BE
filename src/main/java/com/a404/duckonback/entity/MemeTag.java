package com.a404.duckonback.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "meme_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_meme_tag", columnNames = {"meme_id", "tag_id"}),
        indexes = {
                @Index(name = "idx_meme_tag_meme", columnList = "meme_id"),
                @Index(name = "idx_meme_tag_tag", columnList = "tag_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemeTag {

    @EmbeddedId
    private MemeTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("memeId")
    @JoinColumn(name = "meme_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_meme_tag_meme"))
    private Meme meme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_meme_tag_tag"))
    private Tag tag;

    /** 편의 생성자 */
    public static MemeTag of(Meme meme, Tag tag) {
        MemeTag mt = new MemeTag();
        mt.setMeme(meme);
        mt.setTag(tag);
        mt.setId(new MemeTagId(meme.getId(), tag.getId()));
        return mt;
    }
}
