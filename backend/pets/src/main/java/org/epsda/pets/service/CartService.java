package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.AddToCartDTO;
import org.epsda.pets.pojo.dto.CartChangeDTO;
import org.epsda.pets.pojo.dto.CartDetailDTO;
import org.epsda.pets.pojo.vo.CartChangeVO;
import org.epsda.pets.pojo.vo.CartProductDetailVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.PreOrderDetailVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/16
 * Time: 12:01
 *
 * @Author: 憨八嘎
 */
public interface CartService {
    PageVO<CartProductDetailVO> list(CartDetailDTO cartDetailDTO);

    Boolean add(AddToCartDTO addToCartDTO);

    Long getCounts(@NotNull Long userId);

    CartChangeVO change(CartChangeDTO cartChangeDTO);

    List<PreOrderDetailVO> batchSettle(@NotNull List<Long> cartIds);

    PreOrderDetailVO settle(@NotNull Long cartId);

    Boolean delete(@NotNull Long cartId);

    List<Boolean> batchDelete(List<Long> cartIds);
}
