package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import com.app.usochicamochabackend.review.application.dto.ImagesDTO;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ImagesMapper {

    private ImagesMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ImageDTO toDto(ImageEntity entity) {
        if (entity == null) return null;

        return new ImageDTO(
                entity.getUrl(),
                entity.getUuid(),
                entity.getInspection() != null ? entity.getInspection().getId() : null
        );
    }

    public static ImagesDTO toDtoList(List<ImageEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ImagesDTO(List.of());
        }

        List<ImageDTO> imageDTOList = entities.stream()
                .map(ImagesMapper::toDto)
                .collect(Collectors.toList());

        return new ImagesDTO(imageDTOList);
    }
}
