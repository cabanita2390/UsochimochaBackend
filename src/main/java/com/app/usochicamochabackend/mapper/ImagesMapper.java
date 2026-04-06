package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;

import java.util.Collections;
import java.util.List;

public class ImagesMapper {

    private ImagesMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ImageDTO toDto(ImageEntity image) {
        if (image == null) {
            return null;
        }
        return new ImageDTO(image.getId(), image.getUrl());
    }

    public static List<ImageDTO> toDtoList(List<ImageEntity> images) {
        if (images == null) {
            return Collections.emptyList();
        }
        return images.stream().map(ImagesMapper::toDto).toList();
    }
}
