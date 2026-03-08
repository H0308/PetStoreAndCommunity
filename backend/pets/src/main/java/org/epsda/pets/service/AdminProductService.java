package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/29
 * Time: 18:55
 *
 * @Author: 憨八嘎
 */
public interface AdminProductService {
    PageVO<ProductListVO> list(ProductListDTO productListDTO, ProductFilterDTO productFilterDTO);

    AdminPetDetailVO getPet(Long productId, Long userId);

    AdminSupplyDetailVO getSupply(Long productId, Long userId);

    ProductChangeVO change(MainProductChangeDTO mainProductChangeDTO, List<MultipartFile> files, List<Integer> mainFlags);

    ProductAddVO addPet(PetProductAddDTO petProductAddDTO, List<MultipartFile> files, List<Integer> mainFlags);

    ProductAddVO addSupply(SupplyProductAddDTO supplyProductAddDTO, List<MultipartFile> files, List<Integer> mainFlags);

    Boolean offShelf(Long productId, Long userId);

    Boolean onSelling(Long productId, Long userId);

    Boolean delete(Long productId, Long userId);

    Boolean batchDelete(List<Long> productIds, Long userId);
}
