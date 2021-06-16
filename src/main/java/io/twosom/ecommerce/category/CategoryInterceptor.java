package io.twosom.ecommerce.category;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class CategoryInterceptor implements HandlerInterceptor {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;


    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {

        // TODO HandlerInterceptor 이용하여 View 렌더링 전에 Model 에 카테고리 정보들 담기
        if (modelAndView != null) {
//            List<Category> categories = categoryRepository.findAllByParentCategoryIsNullAndPublish(true);
            List<CategoryDto> categories = categoryRepository.findAllByParentCategoryIsNullAndPublish(true)
                    .stream()
                    .map(category -> modelMapper.map(category, CategoryDto.class))
                    .collect(Collectors.toList());
            modelAndView.addObject("categories", categories);
        }
    }


}
