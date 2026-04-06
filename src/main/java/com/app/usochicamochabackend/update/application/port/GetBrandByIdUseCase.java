package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.update.application.dto.BrandResponse;

public interface GetBrandByIdUseCase {
    BrandResponse getBrandById(Long id);
}
