package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.executor.BatchResult;
import org.apache.tomcat.util.bcel.Const;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.bo.PetProductBO;
import org.epsda.pets.pojo.bo.ProductCommentBO;
import org.epsda.pets.pojo.bo.SupplyProductBO;
import org.epsda.pets.pojo.document.ProductDocument;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.repository.ProductRepository;
import org.epsda.pets.service.AdminProductService;
import org.epsda.pets.service.PetService;
import org.epsda.pets.service.SupplyService;
import org.epsda.pets.utils.BaiduMapUtil;
import org.epsda.pets.utils.GenerateIdentifierNumber;
import org.epsda.pets.utils.OSSUploadFileUtil;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/29
 * Time: 18:56
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminProductServiceImpl implements AdminProductService {

    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private ProductCategorySuperMapper productCategorySuperMapper;
    @Autowired
    private ProductCategorySubMapper productCategorySubMapper;
    @Autowired
    private ProductImageMapper productImageMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private PetSubMapper petSubMapper;
    @Autowired
    private PetSupplySubMapper petSupplySubMapper;
    @Autowired
    private CommentSuperMapper commentSuperMapper;
    @Autowired
    private ProductCommentSubMapper productCommentSubMapper;
    @Autowired
    private BaiduMapUtil baiduMapUtil;
    @Autowired
    private OSSUploadFileUtil uploadFileUtil;
    @Autowired
    private CommentMediaMapper commentMediaMapper;
    @Autowired
    private ProductRepository productRepository;

    @Override
    public PageVO<ProductListVO> list(ProductListDTO productListDTO, ProductFilterDTO productFilterDTO) {
        if (productListDTO == null) {
            throw new PetException("商品信息错误，获取商品列表失败");
        }

        Long currentPage = productListDTO.getCurrentPage();
        Long pageSize = productListDTO.getPageSize();

        Page<ProductSuper> productSuperPage = new Page<>(currentPage, pageSize);

        LambdaQueryWrapper<ProductSuper> productSuperLambdaQueryWrapper = this.buildPageFilterCondition(productFilterDTO);

        productSuperLambdaQueryWrapper.eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG);

        Page<ProductSuper> productSuperPages = productSuperMapper.selectPage(productSuperPage, productSuperLambdaQueryWrapper);
        List<ProductSuper> records = productSuperPages.getRecords();
        List<ProductListVO> productListVOS = new ArrayList<>();
        if (records != null) {
            records.forEach(productSuper ->
                    productListVOS.add(this.buildProductListVOFromProductSuper(productSuper)));
        }

        return PageVO.<ProductListVO>builder()
                .currentPage(productSuperPages.getCurrent())
                .totalPages(productSuperPages.getPages())
                .totalCount(productSuperPages.getTotal())
                .totalRecords(productListVOS)
                .build();
    }

    @Override
    public AdminPetDetailVO getPet(Long productId, Long userId) {
        if (productId == null || userId == null) {
            throw new PetException("商品信息错误，获取商品详情失败");
        }

        SecurityUtil.checkAdmin(userId);

        PetProductBO petProductBO = productSuperMapper.selectPetByProductId(productId);
        if (petProductBO == null || petProductBO.getPetSub() == null
                || petProductBO.getProductSuper() == null) {
            throw new PetException("指定商品不存在，获取商品详情失败");
        }

        ProductSuper productSuper = petProductBO.getProductSuper();
        PetSub petSub = petProductBO.getPetSub();

        if (!productSuper.getType().equals(Constants.PET)) {
            throw new PetException("当前商品并非宠物，获取商品详情失败");
        }

        AdminBaseProductDetailVO adminBaseProductDetailVO =
                this.buildBaseProductDetailFromProductSuper(productSuper);

        AdminPetDetailVO adminPetDetailVO = new AdminPetDetailVO(adminBaseProductDetailVO);
        adminPetDetailVO.setHealthStatus(petSub.getHealthStatus());
        adminPetDetailVO.setTrainNote(petSub.getTrainNote());
        adminPetDetailVO.setRaiseNote(petSub.getRaiseNote());
        adminPetDetailVO.setVaccineFlag(petSub.getVaccineFlag());

        return adminPetDetailVO;
    }

    @Override
    public AdminSupplyDetailVO getSupply(Long productId, Long userId) {
        if (productId == null || userId == null) {
            throw new PetException("商品信息错误，获取商品详情失败");
        }

        SecurityUtil.checkAdmin(userId);

        SupplyProductBO supplyProductBO = productSuperMapper.selectSupplyByProductId(productId);
        if (supplyProductBO == null || supplyProductBO.getPetSupplySub() == null
                || supplyProductBO.getProductSuper() == null) {
            throw new PetException("指定商品不存在，获取商品详情失败");
        }

        ProductSuper productSuper = supplyProductBO.getProductSuper();
        PetSupplySub petSupplySub = supplyProductBO.getPetSupplySub();

        if (!productSuper.getType().equals(Constants.SUPPLY)) {
            throw new PetException("当前商品并非宠物用品，获取商品详情失败");
        }

        AdminBaseProductDetailVO adminBaseProductDetailVO =
                this.buildBaseProductDetailFromProductSuper(productSuper);

        AdminSupplyDetailVO adminSupplyDetailVO = new AdminSupplyDetailVO(adminBaseProductDetailVO);
        adminSupplyDetailVO.setBrand(petSupplySub.getBrand());
        adminSupplyDetailVO.setCompany(petSupplySub.getCompany());
        adminSupplyDetailVO.setManufactureDate(petSupplySub.getManufactureDate());
        adminSupplyDetailVO.setGuaranteeDate(petSupplySub.getGuaranteeDate());
        adminSupplyDetailVO.setFitAge(petSupplySub.getFitAge());
        adminSupplyDetailVO.setFitVariety(petSupplySub.getFitVariety());

        return adminSupplyDetailVO;
    }

    @Transactional
    @Override
    public ProductChangeVO change(MainProductChangeDTO mainProductChangeDTO, List<MultipartFile> files, List<Integer> mainFlags) {
        if (mainProductChangeDTO == null) {
            throw new PetException("修改信息错误，无法继续修改");
        }

        BaseProductChangeDTO baseProductChangeDTO = mainProductChangeDTO.getBaseProductChangeDTO();
        PetProductChangeDTO petProductChangeDTO = mainProductChangeDTO.getPetProductChangeDTO();
        SupplyProductChangeDTO supplyProductChangeDTO = mainProductChangeDTO.getSupplyProductChangeDTO();

        Long productId = mainProductChangeDTO.getProductId();
        Long userId = mainProductChangeDTO.getUserId();

        if (productId == null || userId == null) {
            throw new PetException("基础信息错误，修改失败");
        }

        ProductSuper existed = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getId, productId)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (existed == null) {
            throw new PetException("指定的商品不存在，无法修改");
        }

        SecurityUtil.checkAdmin(userId);

        ProductDocument productDocument = productRepository.findProductDocumentById(productId);
        // 先检测是否发生了类型修改
        // 一旦发生类型修改，则需要清除子表的信息
        Integer type = 0;
        if (baseProductChangeDTO != null && baseProductChangeDTO.getType() != null) {
            type = baseProductChangeDTO.getType();
            Long mainCategoryId = baseProductChangeDTO.getMainCategoryId();
            Long subCategoryId = baseProductChangeDTO.getSubCategoryId();
            ProductCategorySuper productCategorySuper = productCategorySuperMapper.selectOne(new LambdaQueryWrapper<ProductCategorySuper>()
                    .eq(ProductCategorySuper::getId, mainCategoryId)
                    .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

            if (productCategorySuper == null || !productCategorySuper.getType().equals(type)) {
                throw new PetException("指定分类不存在或者指定的分类类型与商品类型不符，修改失败");
            }

            boolean typeDeleteRet = true;
            boolean insertRet = true;
            if (type.equals(Constants.PET) && petProductChangeDTO != null) {
                typeDeleteRet = petSupplySubMapper.delete(new LambdaQueryWrapper<PetSupplySub>().eq(PetSupplySub::getProductId, productId)) == 1;
                // 接着向另外一张子表插入所有数据
                PetSub petSub = new PetSub();
                petSub.setProductId(productId);
                petSub.setHealthStatus(petProductChangeDTO.getHealthStatus());
                petSub.setTrainNote(petProductChangeDTO.getTrainNote());
                petSub.setRaiseNote(petProductChangeDTO.getRaiseNote());
                petSub.setVaccineFlag(petProductChangeDTO.getVaccineFlag());
                insertRet = petSubMapper.insert(petSub) == 1;
            } else if (type.equals(Constants.SUPPLY) && supplyProductChangeDTO != null) {
                typeDeleteRet = petSubMapper.delete(new LambdaQueryWrapper<PetSub>().eq(PetSub::getProductId, productId)) == 1;
                PetSupplySub petSupplySub = new PetSupplySub();
                petSupplySub.setProductId(productId);
                petSupplySub.setBrand(supplyProductChangeDTO.getBrand());
                petSupplySub.setCompany(supplyProductChangeDTO.getCompany());
                petSupplySub.setFitAge(supplyProductChangeDTO.getFitAge());
                petSupplySub.setFitVariety(supplyProductChangeDTO.getFitVariety());
                LocalDateTime manufactureDate = supplyProductChangeDTO.getManufactureDate();
                LocalDateTime guaranteeDate = supplyProductChangeDTO.getGuaranteeDate();
                if (guaranteeDate.isBefore(manufactureDate)) {
                    throw new PetException("保质期不得早于生产日期");
                }
                petSupplySub.setManufactureDate(manufactureDate);
                petSupplySub.setGuaranteeDate(guaranteeDate);
                insertRet = petSupplySubMapper.insert(petSupplySub) == 1;
            }

            if (!typeDeleteRet) {
                throw new PetException("原类型数据删除错误");
            }

            if (!insertRet) {
                throw new PetException("新类型数据插入错误");
            }

            // 最后更新类型以及新的分类
            boolean updateRet = productSuperMapper.update(new LambdaUpdateWrapper<ProductSuper>()
                    .eq(ProductSuper::getId, productId)
                    .set(ProductSuper::getType, type)
                    .set(ProductSuper::getMainCategoryId, mainCategoryId)
                    .set(ProductSuper::getSubCategoryId, subCategoryId)) == 1;
            if (!updateRet) {
                throw new PetException("类型和分类更新错误");
            }

            productDocument.setProductType(type);
        }

        // 进行基础信息修改
        // 此时需要分两步：公共信息、字表信息
        boolean superUpdateRet = true;
        if (baseProductChangeDTO != null && (StringUtils.hasText(baseProductChangeDTO.getProductName()) ||
                StringUtils.hasText(baseProductChangeDTO.getDescription()) ||
                baseProductChangeDTO.getPrice() != null ||
                baseProductChangeDTO.getStock() != null)) {
            String productName = baseProductChangeDTO.getProductName();
            String description = baseProductChangeDTO.getDescription();
            BigDecimal price = baseProductChangeDTO.getPrice();
            if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new PetException("商品价格必须大于0");
            }
            Long stock = baseProductChangeDTO.getStock();
            if (stock != null && stock < 1) {
                throw new PetException("库存量至少为1");
            }
            superUpdateRet = productSuperMapper.update(new LambdaUpdateWrapper<ProductSuper>()
                    .eq(ProductSuper::getId, productId)
                    .set(StringUtils.hasText(productName), ProductSuper::getName, productName)
                    .set(StringUtils.hasText(description), ProductSuper::getDescription, description)
                    .set(price != null, ProductSuper::getPrice, price)
                    .set(stock != null, ProductSuper::getStock, stock)) == 1;
            if (stock != null && stock > 1) {
                // 需要更新商品状态
                productSuperMapper.update(new LambdaUpdateWrapper<ProductSuper>()
                        .eq(ProductSuper::getId, productId)
                        .set(ProductSuper::getStatus, Constants.ON_SELLING));
            }
            if (StringUtils.hasText(productName)) {
                productDocument.setProductName(productName);
            }
        }
        if (!superUpdateRet) {
            throw new PetException("商品基础信息修改失败");
        }
        if (baseProductChangeDTO != null && StringUtils.hasText(baseProductChangeDTO.getShipAddress())) {
            // 删除原有地址
            Address deletedAddress = new Address();
            deletedAddress.setId(existed.getShipId());
            deletedAddress.setDeleteFlag(Constants.DELETED_FLAG);
            boolean addressUpdateRet = addressMapper.updateById(deletedAddress) == 1;
            if (!addressUpdateRet) {
                throw new PetException("原商品发货地址删除失败");
            }

            // 新增地址
            String shipAddress = baseProductChangeDTO.getShipAddress();
            String mapJson = baiduMapUtil.geographyEncoding(shipAddress);
            List<String> latitudeAndLongitude = baiduMapUtil.getLatitudeAndLongitude(mapJson);
            Address address = new Address();
            address.setLatitude(latitudeAndLongitude.get(0));
            address.setLongitude(latitudeAndLongitude.get(1));
            address.setAddressText(shipAddress);
            boolean addressInsertRet = addressMapper.insert(address) == 1;
            if (!addressInsertRet) {
                throw new PetException("发货地址新增失败");
            }
            addressUpdateRet = productSuperMapper.update(new LambdaUpdateWrapper<ProductSuper>()
                    .eq(ProductSuper::getId, productId).set(ProductSuper::getShipId, address.getId())) == 1;
            if (!addressUpdateRet) {
                throw new PetException("新商品发货地址修改失败");
            }
        }

        Integer existedType = existed.getType();
        boolean subUpdateRet = true;
        if (type == 0 && existedType.equals(Constants.PET) && petProductChangeDTO != null) {
            Integer healthStatus = petProductChangeDTO.getHealthStatus();
            String raiseNote = petProductChangeDTO.getRaiseNote();
            String trainNote = petProductChangeDTO.getTrainNote();
            Integer vaccineFlag = petProductChangeDTO.getVaccineFlag();
            subUpdateRet = petSubMapper.update(new LambdaUpdateWrapper<PetSub>()
                    .eq(PetSub::getProductId, productId)
                    .set(healthStatus != null, PetSub::getHealthStatus, healthStatus)
                    .set(StringUtils.hasText(raiseNote), PetSub::getRaiseNote, raiseNote)
                    .set(StringUtils.hasText(trainNote), PetSub::getTrainNote, trainNote)
                    .set(vaccineFlag != null, PetSub::getVaccineFlag, vaccineFlag)) == 1;
        } else if (existedType.equals(Constants.SUPPLY) && supplyProductChangeDTO != null) {
            String brand = supplyProductChangeDTO.getBrand();
            String company = supplyProductChangeDTO.getCompany();
            String fitAge = supplyProductChangeDTO.getFitAge();
            String fitVariety = supplyProductChangeDTO.getFitVariety();
            LocalDateTime manufactureDate = supplyProductChangeDTO.getManufactureDate();
            LocalDateTime guaranteeDate = supplyProductChangeDTO.getGuaranteeDate();
            if (guaranteeDate != null && guaranteeDate.isBefore(manufactureDate)) {
                throw new PetException("保质期不得早于生产日期");
            }
            subUpdateRet = petSupplySubMapper.update(new LambdaUpdateWrapper<PetSupplySub>()
                    .eq(PetSupplySub::getProductId, productId)
                    .set(StringUtils.hasText(brand), PetSupplySub::getBrand, brand)
                    .set(StringUtils.hasText(company), PetSupplySub::getCompany, company)
                    .set(StringUtils.hasText(fitAge), PetSupplySub::getFitAge, fitAge)
                    .set(StringUtils.hasText(fitVariety), PetSupplySub::getFitVariety, fitVariety)
                    .set(manufactureDate != null, PetSupplySub::getManufactureDate, manufactureDate)
                    .set(guaranteeDate != null, PetSupplySub::getGuaranteeDate, guaranteeDate)) == 1;
        }

        if (!subUpdateRet) {
            throw new PetException("附加数据更新失败");
        }

        // 如果修改了主分类，则需要先确保当前选择的分类类型与商品类型一致
        if (type == 0 && baseProductChangeDTO != null && baseProductChangeDTO.getMainCategoryId() != null) {
            Long mainCategoryId = baseProductChangeDTO.getMainCategoryId();
            boolean categoryUpdateRet = true;
            ProductCategorySuper productCategorySuper = productCategorySuperMapper.selectOne(new LambdaQueryWrapper<ProductCategorySuper>()
                    .eq(ProductCategorySuper::getId, mainCategoryId)
                    .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

            if (productCategorySuper == null || !productCategorySuper.getType().equals(existedType)) {
                throw new PetException("指定分类不存在或者指定的分类类型与商品类型不符，修改失败");
            }

            categoryUpdateRet = productSuperMapper.update(new LambdaUpdateWrapper<ProductSuper>()
                    .eq(ProductSuper::getId, productId)
                    .set(ProductSuper::getMainCategoryId, mainCategoryId)) == 1;

            if (!categoryUpdateRet) {
                throw new PetException("一级分类修改失败");
            }
        }
        // 先确保主分类是正确合规的，如果主分类错了，二级分类也不可以再修改
        // 否则改的二级分类一定是属于主分类的
        if (type == 0 && baseProductChangeDTO != null && baseProductChangeDTO.getSubCategoryId() != null) {
            superUpdateRet = productSuperMapper.update(new LambdaUpdateWrapper<ProductSuper>()
                    .eq(ProductSuper::getId, productId)
                    .set(ProductSuper::getSubCategoryId, baseProductChangeDTO.getSubCategoryId())) == 1;
        }
        if (!superUpdateRet) {
            throw new PetException("商品基础信息修改失败");
        }

        // 检查是否有需要删除的商品图片
        if (baseProductChangeDTO != null && !baseProductChangeDTO.getProductImageUrls().isEmpty()) {
            Map<Long, Integer> productImageUrls = baseProductChangeDTO.getProductImageUrls();
            List<Long> deleteProductImageIds = new ArrayList<>();
            for (var urlKv : productImageUrls.entrySet()) {
                if (urlKv.getValue().equals(Constants.DELETED_FLAG)) {
                    Long imageId = urlKv.getKey();
                    ProductImage productImage = productImageMapper.selectOne(new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getId, imageId)
                            .eq(ProductImage::getDeleteFlag, Constants.NOT_DELETED_FLAG));

                    if (productImage == null) {
                        throw new PetException("指定商品照片不存在，删除失败");
                    }
                    deleteProductImageIds.add(imageId);
                }
            }
            if (!deleteProductImageIds.isEmpty()) {
                int update = productImageMapper.update(new LambdaUpdateWrapper<ProductImage>()
                        .in(ProductImage::getId, deleteProductImageIds)
                        .set(ProductImage::getDeleteFlag, Constants.DELETED_FLAG));
            }
        }

        // 新增图片
        if (files != null && !files.isEmpty()) {
            boolean updateRet = this.insertProductImage(productId, files, mainFlags);
        }

        // 同步ES数据
        if (baseProductChangeDTO != null && (baseProductChangeDTO.getType() != null ||
            StringUtils.hasText(baseProductChangeDTO.getProductName()))) {
            productRepository.save(productDocument);
        }

        return ProductChangeVO.builder()
                .successFlag(true).productId(productId)
                .build();
    }

    @Transactional
    @Override
    public ProductAddVO addPet(PetProductAddDTO petProductAddDTO, List<MultipartFile> files, List<Integer> mainFlags) {
        if (petProductAddDTO == null) {
            throw new PetException("宠物信息错误，无法新增宠物商品");
        }

        SecurityUtil.checkAdmin(petProductAddDTO.getUserId());

        String productName = petProductAddDTO.getProductName();
        String description = petProductAddDTO.getDescription();
        String shipAddress = petProductAddDTO.getShipAddress();
        Long mainCategoryId = petProductAddDTO.getMainCategoryId();
        Long subCategoryId = petProductAddDTO.getSubCategoryId();
        BigDecimal price = petProductAddDTO.getPrice();
        Long stock = petProductAddDTO.getStock();
        Integer healthStatus = petProductAddDTO.getHealthStatus();
        String trainNote = petProductAddDTO.getTrainNote();
        String raiseNote = petProductAddDTO.getRaiseNote();
        Integer vaccineFlag = petProductAddDTO.getVaccineFlag();

        // 插入父表数据
        Long productSuperId = this.baseProductAdd(productName, shipAddress, mainCategoryId, description,
                subCategoryId, price, stock, Constants.PET);

        ProductSuper setIdentifier = new ProductSuper();
        setIdentifier.setId(productSuperId);
        setIdentifier.setIdentifier(productSuperId + GenerateIdentifierNumber.generateIdentifierNumber());

        boolean updateRet = productSuperMapper.updateById(setIdentifier) == 1;
        if (!updateRet) {
            throw new PetException("更新商品编号失败");
        }

        PetSub petSub = new PetSub();
        petSub.setProductId(productSuperId);
        petSub.setHealthStatus(healthStatus);
        petSub.setTrainNote(trainNote);
        petSub.setRaiseNote(raiseNote);
        petSub.setVaccineFlag(vaccineFlag);

        boolean petSubInsertRet = petSubMapper.insert(petSub) == 1;
        if (!petSubInsertRet) {
            throw new PetException("宠物附加数据插入失败");
        }

        // 新增图片
        boolean imageInsertRet = this.insertProductImage(productSuperId, files, mainFlags);

        return ProductAddVO.builder()
                .successFlag(true).productId(productSuperId)
                .build();
    }

    @Transactional
    @Override
    public ProductAddVO addSupply(SupplyProductAddDTO supplyProductAddDTO, List<MultipartFile> files, List<Integer> mainFlags) {
        if (supplyProductAddDTO == null) {
            throw new PetException("宠物用品信息错误，无法新增宠物用品");
        }

        SecurityUtil.checkAdmin(supplyProductAddDTO.getUserId());

        String productName = supplyProductAddDTO.getProductName();
        String description = supplyProductAddDTO.getDescription();
        String shipAddress = supplyProductAddDTO.getShipAddress();
        Long mainCategoryId = supplyProductAddDTO.getMainCategoryId();
        Long subCategoryId = supplyProductAddDTO.getSubCategoryId();
        BigDecimal price = supplyProductAddDTO.getPrice();
        Long stock = supplyProductAddDTO.getStock();
        String brand = supplyProductAddDTO.getBrand();
        String fitAge = supplyProductAddDTO.getFitAge();
        String fitVariety = supplyProductAddDTO.getFitVariety();
        LocalDateTime manufactureDate = supplyProductAddDTO.getManufactureDate();
        LocalDateTime guaranteeDate = supplyProductAddDTO.getGuaranteeDate();
        String company = supplyProductAddDTO.getCompany();

        // 插入父表数据
        Long productSuperId = this.baseProductAdd(productName, shipAddress, mainCategoryId, description,
                subCategoryId, price, stock, Constants.SUPPLY);

        ProductSuper setIdentifier = new ProductSuper();
        setIdentifier.setId(productSuperId);
        setIdentifier.setIdentifier(productSuperId + GenerateIdentifierNumber.generateIdentifierNumber());

        boolean updateRet = productSuperMapper.updateById(setIdentifier) == 1;
        if (!updateRet) {
            throw new PetException("更新商品编号失败");
        }

        PetSupplySub petSupplySub = new PetSupplySub();
        petSupplySub.setProductId(productSuperId);
        petSupplySub.setFitAge(fitAge);
        petSupplySub.setFitVariety(fitVariety);
        if (guaranteeDate.isBefore(manufactureDate)) {
            throw new PetException("保质期不得早于生产日期");
        }
        petSupplySub.setManufactureDate(manufactureDate);
        petSupplySub.setGuaranteeDate(guaranteeDate);
        petSupplySub.setBrand(brand);
        petSupplySub.setCompany(company);

        boolean petSubInsertRet = petSupplySubMapper.insert(petSupplySub) == 1;
        if (!petSubInsertRet) {
            throw new PetException("宠物用品附加数据插入失败");
        }

        updateRet = this.insertProductImage(productSuperId, files, mainFlags);

        return ProductAddVO.builder()
                .successFlag(true).productId(productSuperId)
                .build();
    }

    @Override
    public Boolean offShelf(Long productId, Long userId) {
        if (productId == null || userId == null) {
            throw new PetException("商品信息错误，无法下架商品");
        }

        SecurityUtil.checkAdmin(userId);

        ProductSuper existed = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getId, productId)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
        );
        if (existed == null) {
            throw new PetException("指定商品不存在，无法下架");
        }

        if (existed.getStatus().equals(Constants.OFF_SHELF)) {
            throw new PetException("商品已下架，无法再下架");
        }

        ProductSuper productSuper = new ProductSuper();
        productSuper.setId(existed.getId());
        productSuper.setStatus(Constants.OFF_SHELF);

        boolean updateRet = productSuperMapper.updateById(productSuper) == 1;
        if (!updateRet) {
            throw new PetException("商品下架失败");
        }

        ProductDocument productDocument = productRepository.findProductDocumentById(existed.getId());
        if (productDocument != null) {
            productRepository.delete(productDocument);
        }

        return true;
    }

    @Override
    public Boolean onSelling(Long productId, Long userId) {
        if (productId == null || userId == null) {
            throw new PetException("商品信息错误，无法上架商品");
        }

        SecurityUtil.checkAdmin(userId);

        ProductSuper existed = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getId, productId)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
        );

        if (existed == null) {
            throw new PetException("指定商品不存在，无法上架");
        }

        if (existed.getStatus().equals(Constants.ON_SELLING)) {
            throw new PetException("商品已上架，无法再上架");
        }

        Long stock = existed.getStock();
        ProductSuper productSuper = new ProductSuper();
        productSuper.setId(existed.getId());
        if (stock > 0) {
            productSuper.setStatus(Constants.ON_SELLING);
        } else if (stock == 0){
            productSuper.setStatus(Constants.SOLD_OUT);
        }

        boolean updateRet = productSuperMapper.updateById(productSuper) == 1;
        if (!updateRet) {
            throw new PetException("商品上架失败");
        }

        List<ProductImage> productImages = productImageMapper.selectImagesByProductId(productId);
        ProductDocument productDocument = new ProductDocument();
        productDocument.setId(productId);
        productDocument.setProductName(existed.getName());
        productDocument.setProductType(existed.getType());
        productDocument.setProductImageUrl(productImages.get(0).getImageUrl());
        productDocument.setCreateTime(existed.getCreateTime());
        productRepository.save(productDocument);

        return true;
    }

    @Transactional
    @Override
    public Boolean delete(Long productId, Long userId) {
        if (productId == null || userId == null) {
            throw new PetException("商品信息错误，无法删除商品");
        }

        SecurityUtil.checkAdmin(userId);

        ProductSuper existed = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getId, productId)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
        );

        if (existed == null) {
            throw new PetException("指定商品不存在，无法删除");
        }

        if (!existed.getStatus().equals(Constants.OFF_SHELF)) {
            throw new PetException("指定商品未下架，无法删除");
        }

        Integer type = existed.getType();
        ProductSuper productSuper = new ProductSuper();
        productSuper.setId(existed.getId());
        productSuper.setDeleteFlag(Constants.DELETED_FLAG);
        boolean superUpdateRet = productSuperMapper.updateById(productSuper) == 1;
        if (!superUpdateRet) {
            throw new PetException("商品总表数据删除失败");
        }

        boolean subDeleteRet = true;
        if (type.equals(Constants.PET)) {
            subDeleteRet = petSubMapper.delete(new LambdaQueryWrapper<PetSub>()
                    .eq(PetSub::getProductId, existed.getId())) == 1;

        } else if (type.equals(Constants.SUPPLY)) {
            PetSupplySub petSupplySub = new PetSupplySub();
            petSupplySub.setProductId(existed.getId());
            subDeleteRet = petSupplySubMapper.delete(new LambdaQueryWrapper<PetSupplySub>()
                    .eq(PetSupplySub::getProductId, existed.getId())) == 1;
        }
        if (!subDeleteRet) {
            throw new PetException("商品附加数据删除失败");
        }

        // 将对应商品下的评论全部删除
        List<ProductCommentBO> productCommentBOS = commentSuperMapper.selectProductCommentsByObjectId(existed.getId());
        List<Long> superDeletedIds = productCommentBOS.stream()
                .filter(productCommentBO ->
                    productCommentBO.getCommentSuper().getDeleteFlag().equals(Constants.NOT_DELETED_FLAG))
                .map(productCommentBO ->
                    productCommentBO.getCommentSuper().getId()).toList();
        List<Long> mediaDeleteIds = new ArrayList<>();
        int superDeleteRows = 0;
        int mediaDeleteRows = 0;
        if (!superDeletedIds.isEmpty()) {
            superDeleteRows = commentSuperMapper.update(new LambdaUpdateWrapper<CommentSuper>()
                    .in(CommentSuper::getId, superDeletedIds)
                    .set(CommentSuper::getDeleteFlag, Constants.DELETED_FLAG));
            // 同时删除评论对应的媒体（有的评论可能没有媒体）
            List<CommentMedia> commentMedia = commentMediaMapper.selectList(new LambdaQueryWrapper<CommentMedia>()
                    .in(CommentMedia::getCommentId, superDeletedIds)
                    .eq(CommentMedia::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            mediaDeleteIds = commentMedia.stream().map(CommentMedia::getId).toList();
            if (!mediaDeleteIds.isEmpty()) {
                mediaDeleteRows = commentMediaMapper.update(new LambdaUpdateWrapper<CommentMedia>()
                        .in(CommentMedia::getId, mediaDeleteIds)
                        .set(CommentMedia::getDeleteFlag, Constants.DELETED_FLAG));
            }
        }

        if (superDeleteRows != superDeletedIds.size()) {
            throw new PetException("存在商品评论删除失败");
        }

        if (mediaDeleteRows != mediaDeleteIds.size()) {
            throw new PetException("存在商品媒体删除失败");
        }

        // 删除商品评论子表的数据
        List<Long> subDeletedIds = productCommentBOS.stream()
                .filter(productCommentBO -> productCommentBO.getProductCommentSub() != null)
                .map(productCommentBO ->
                        productCommentBO.getProductCommentSub().getSubId()).toList();

        int subDeleteRows = 0;
        if (!subDeletedIds.isEmpty()) {
            subDeleteRows = productCommentSubMapper.deleteByIds(subDeletedIds);
        }

        // 为了保证用户购物车和订单中可以正确显示商品的图片，此处不删除商品图片

        // 删除当前商品对应的发货地址
        Address address = new Address();
        address.setId(existed.getShipId());
        address.setDeleteFlag(Constants.DELETED_FLAG);
        boolean addressUpdateRet = addressMapper.updateById(address) == 1;
        if (!addressUpdateRet) {
            throw new PetException("商品发货地址删除失败");
        }

        // 删除ES
        ProductDocument productDocument = productRepository.findProductDocumentById(productId);
        if (productDocument != null) {
            productRepository.delete(productDocument);
        }

        return true;
    }

    @Transactional
    @Override
    public Boolean batchDelete(List<Long> productIds, Long userId) {
        if (productIds == null || userId == null) {
            throw new PetException("删除信息错误，无法进行批量删除");
        }

        SecurityUtil.checkAdmin(userId);

        // 先将指定商品设置为下架
        productSuperMapper.update(new LambdaUpdateWrapper<ProductSuper>()
                .in(ProductSuper::getId, productIds)
                .ne(ProductSuper::getStatus, Constants.OFF_SHELF)
                .set(ProductSuper::getStatus, Constants.OFF_SHELF));

        for (Long productId : productIds) {
            Boolean delete = this.delete(productId, userId);
            if (!delete) {
                throw new PetException("当前商品删除失败，停止删除");
            }
        }

        return true;
    }

    private boolean insertProductImage(Long productSuperId, List<MultipartFile> files, List<Integer> mainFlags) {
        if (files == null || files.isEmpty() ||
            mainFlags == null || mainFlags.isEmpty()) {
            throw new PetException("上传的文件不合法");
        }

        List<ProductImageDTO> productImageDTOS = new ArrayList<>();
        if (files.size() != mainFlags.size()) {
            throw new PetException("图片和标记数量不一致，文件错误");
        }
        for (int i = 0; i < files.size(); i++) {
            ProductImageDTO productImageDTO =
                    new ProductImageDTO(files.get(i), mainFlags.get(i));
            productImageDTOS.add(productImageDTO);
        }

        // 限制商品图片最多不超过9张，加一张主图一共10张
        if (productImageDTOS.size() > 10) {
            throw new PetException("图片数量最多不能超过10张");
        }

        // 筛选出主图数量
        List<ProductImageDTO> mainImages = productImageDTOS.stream().filter(productImageDTO ->
                productImageDTO.getIsMain().equals(1)).toList();
        if (mainImages.size() > 1) {
            throw new PetException("主图数量最多1张");
        }
        // 筛选出非主图
        List<ProductImageDTO> notMainImages =productImageDTOS.stream().filter(productImageDTO ->
                !productImageDTO.getIsMain().equals(1)).toList();
        if (notMainImages.size() > 9) {
            throw new PetException("非主图数量最多9张");
        }

        List<ProductImage> productImages = new ArrayList<>();
        productImageDTOS.forEach(productImageDTO -> {
            String fileUrl = uploadFileUtil.uploadFileToOss(productImageDTO.getFile());
            ProductImage productImage = new ProductImage();
            productImage.setProductId(productSuperId);
            productImage.setImageUrl(fileUrl);
            productImage.setMainFlag(productImageDTO.getIsMain());
            productImages.add(productImage);
        });

        List<BatchResult> insert = productImageMapper.insert(productImages);

        ProductDocument productDocument = productRepository.findProductDocumentById(productSuperId);
        productDocument.setProductImageUrl(productImages.get(0).getImageUrl());
        productRepository.save(productDocument);

        return true;
    }

    public AdminBaseProductDetailVO buildBaseProductDetailFromProductSuper(ProductSuper productSuper) {
        ProductImage mainImage = productImageMapper.selectOne(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productSuper.getId())
                .eq(ProductImage::getMainFlag, Constants.MAIN_IMAGE_FLAG)
                .eq(ProductImage::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        List<ProductImage> productImages = productImageMapper.selectImagesByProductId(productSuper.getId());
        Map<Long, String> productImagesUrl = new HashMap<>();
        if (mainImage != null) {
            productImagesUrl.put(mainImage.getId(), mainImage.getImageUrl());
        }
        productImages.forEach(productImage -> productImagesUrl.put(productImage.getId(), productImage.getImageUrl()));
        // 获取分类
        ProductCategorySuper productCategorySuper = productCategorySuperMapper.selectCategoryByCategoryId(productSuper.getMainCategoryId());
        ProductCategorySub productCategorySub = productCategorySubMapper.selectSubCategoryByCategoryId(productSuper.getSubCategoryId());
        // 获取发货地址
        Address address = addressMapper.selectById(productSuper.getShipId());

        return AdminBaseProductDetailVO.builder()
                .identifier(productSuper.getIdentifier()).type(productSuper.getType())
                .shipAddress(address.getAddressText()).productName(productSuper.getName())
                .description(productSuper.getDescription()).status(productSuper.getStatus())
                .stock(productSuper.getStock()).mainCategoryId(productCategorySuper.getId())
                .subCategoryId(productCategorySub.getId()).price(productSuper.getPrice())
                .productImageUrls(productImagesUrl)
                .build();
    }

    public Long baseProductAdd(String productName, String shipAddress, Long mainCategoryId, String description,
                               Long subCategoryId, BigDecimal price, Long stock, Integer type) {
        String mapJson = baiduMapUtil.geographyEncoding(shipAddress);
        List<String> latitudeAndLongitude = baiduMapUtil.getLatitudeAndLongitude(mapJson);
        Address address = new Address();
        address.setLatitude(latitudeAndLongitude.get(0));
        address.setLongitude(latitudeAndLongitude.get(1));
        address.setAddressText(shipAddress);
        boolean addressInsertRet = addressMapper.insert(address) == 1;
        if (!addressInsertRet) {
            throw new PetException("发货地址新增失败");
        }

        ProductSuper productSuper = new ProductSuper();
        productSuper.setName(productName);
        productSuper.setDescription(description);
        productSuper.setType(type);
        productSuper.setShipId(address.getId());
        ProductCategorySuper productCategorySuper = productCategorySuperMapper.selectOne(new LambdaQueryWrapper<ProductCategorySuper>()
                .eq(ProductCategorySuper::getId, mainCategoryId)
                .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
        );
        if (productCategorySuper != null && !productCategorySuper.getType().equals(type)) {
            throw new PetException("第二步的商品分类与第一步选择的类型不符，无法新增商品");
        }
        productSuper.setMainCategoryId(mainCategoryId);
        productSuper.setSubCategoryId(subCategoryId);
        productSuper.setPrice(price);
        productSuper.setStock(stock);

        boolean productSuperInsertRet = productSuperMapper.insert(productSuper) == 1;
        if (!productSuperInsertRet) {
            throw new PetException("宠物基础信息插入失败");
        }

        ProductDocument productDocument = new ProductDocument();
        productDocument.setId(productSuper.getId());
        productDocument.setProductName(productName);
        productDocument.setProductType(type);
        productDocument.setCreateTime(productSuper.getCreateTime());
        productRepository.save(productDocument);

        return productSuper.getId();
    }

    public LambdaQueryWrapper<ProductSuper> buildPageFilterCondition(ProductFilterDTO productFilterDTO) {
        if (productFilterDTO == null) {
            return null;
        }

        Long productId = productFilterDTO.getProductId();
        Integer type = productFilterDTO.getType();
        Integer status = productFilterDTO.getStatus();
        String keyword = productFilterDTO.getKeyword();
        Long mainCategoryId = productFilterDTO.getMainCategoryId();
        Long subCategoryId = productFilterDTO.getSubCategoryId();

        LambdaQueryWrapper<ProductSuper> productSuperLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            productSuperLambdaQueryWrapper.eq(ProductSuper::getId, productId);
        }

        if (type != null && type.equals(Constants.PET)) {
            productSuperLambdaQueryWrapper.eq(ProductSuper::getType, Constants.PET);
        } else if (type != null && type.equals(Constants.SUPPLY)) {
            productSuperLambdaQueryWrapper.eq(ProductSuper::getType, Constants.SUPPLY);
        }

        if (status != null && status.equals(Constants.ON_SELLING)) {
            productSuperLambdaQueryWrapper.eq(ProductSuper::getStatus, Constants.PET);
        } else if (status != null && status.equals(Constants.SOLD_OUT)) {
            productSuperLambdaQueryWrapper.eq(ProductSuper::getStatus, Constants.SOLD_OUT);
        } else if (status != null && status.equals(Constants.OFF_SHELF)) {
            productSuperLambdaQueryWrapper.eq(ProductSuper::getStatus, Constants.OFF_SHELF);
        }

        if (StringUtils.hasText(keyword)) {
            productSuperLambdaQueryWrapper.like(StringUtils.hasText(keyword), ProductSuper::getName, keyword);
        }

        if (mainCategoryId != null) {
            productSuperLambdaQueryWrapper.eq(ProductSuper::getMainCategoryId, mainCategoryId);
        }

        if (subCategoryId != null) {
            productSuperLambdaQueryWrapper.eq(ProductSuper::getSubCategoryId, subCategoryId);
        }

        return productSuperLambdaQueryWrapper;
    }

    public ProductListVO buildProductListVOFromProductSuper(ProductSuper productSuper) {
        if (productSuper == null) {
            return null;
        }

        Long productId = productSuper.getId();
        String identifier = productSuper.getIdentifier();
        Integer type = productSuper.getType();
        String productName = productSuper.getName();
        Long mainCategoryId = productSuper.getMainCategoryId();
        Long subCategoryId = productSuper.getSubCategoryId();
        Long shipId = productSuper.getShipId();
        Long stock = productSuper.getStock();
        Integer status = productSuper.getStatus();
        BigDecimal price = productSuper.getPrice();

        ProductCategorySuper productCategorySuper = productCategorySuperMapper.selectOne(new LambdaQueryWrapper<ProductCategorySuper>()
                .eq(ProductCategorySuper::getId, mainCategoryId)
                .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        ProductCategorySub productCategorySub = productCategorySubMapper.selectOne(new LambdaQueryWrapper<ProductCategorySub>()
                .eq(ProductCategorySub::getId, subCategoryId)
                .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        List<ProductImage> productImages = productImageMapper.selectImagesByProductId(productId);
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>().eq(Address::getId, shipId)
                .eq(Address::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        return ProductListVO.builder().productId(productId)
                .productName(productName).identifier(identifier)
                .type(type).mainCategoryName(productCategorySuper.getName())
                .mainCategoryId(mainCategoryId).subCategoryId(subCategoryId)
                .subCategoryName(productCategorySub.getName())
                .shipAddress(address.getAddressText()).status(status)
                .stock(stock).price(price).productImageUrl(productImages.get(0).getImageUrl())
                .build();
    }
}
