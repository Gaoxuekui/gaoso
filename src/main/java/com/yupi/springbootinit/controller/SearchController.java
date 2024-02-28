package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.picture.PictureQueryRequest;
import com.yupi.springbootinit.model.dto.post.PostQueryRequest;
import com.yupi.springbootinit.model.dto.search.SearchRequest;
import com.yupi.springbootinit.model.dto.user.UserQueryRequest;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.vo.PostVO;
import com.yupi.springbootinit.model.vo.SearchVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.PictureService;
import com.yupi.springbootinit.service.PostService;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
* 图片接口
*
* @author <a href="https://github.com/liyupi">程序员鱼皮</a>
* @from <a href="https://yupi.icu">编程导航知识星球</a>
*/
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

   @Resource
   private PictureService pictureService;
   @Resource
   private UserService userService;
   @Resource
   private PostService postService;

   @PostMapping("/all")
   public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest,HttpServletRequest request) {
      String searchText = searchRequest.getSearchText();

      CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
         UserQueryRequest userQueryRequest = new UserQueryRequest();
         userQueryRequest.setUserName(searchText);
         Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);
          return userVOPage;
      });

      CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
         PostQueryRequest postQueryRequest = new PostQueryRequest();
         postQueryRequest.setSearchText(searchText);
         Page<PostVO> postVOPage = postService.listPostVOByPage(postQueryRequest, request);

         return postVOPage;
      });

      CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
         Page<Picture> picturePage = pictureService.seachPicture(searchText, 1, 10);
         return picturePage;
      });

      CompletableFuture.allOf(userTask,postTask,pictureTask).join();

      try {
         Page<UserVO> userVOPage = userTask.get();
         Page<PostVO> postVOPage = postTask.get();
         Page<Picture> picturePage = pictureTask.get();
         SearchVO searchVO = new SearchVO();
         searchVO.setPostList(postVOPage.getRecords());
         searchVO.setPictureList(picturePage.getRecords());
         searchVO.setUserList(userVOPage.getRecords());
         return ResultUtils.success(searchVO);
      }catch (Exception e) {
      log.error("查询异常",e);
         throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询异常");
      }



   }



}
