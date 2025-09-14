package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.update.application.dto.BrandResponse;

import java.util.List;

public interface GetAllBrandsUseCase {
    List<BrandResponse> getAllBrands();
}
