package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.update.application.dto.BrandRequest;
import com.app.usochicamochabackend.update.application.dto.BrandResponse;

public interface CreateBrandUseCase {
    BrandResponse createBrand(BrandRequest brand);
}
