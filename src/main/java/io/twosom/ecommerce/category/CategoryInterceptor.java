package io.twosom.ecommerce.category;

import io.twosom.ecommerce.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

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

        if (modelAndView != null && !isRedirect(modelAndView)) {

            List<CategoryDto> categories = categoryRepository.findAllByParentCategoryIsNullAndPublish(true)
                    .stream()
                    .map(category -> modelMapper.map(category, CategoryDto.class))
                    .collect(Collectors.toList());
            modelAndView.addObject("categories", categories);
        }
    }

    private boolean isRedirect(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect:") || modelAndView.getView() instanceof RedirectView;
    }


}
