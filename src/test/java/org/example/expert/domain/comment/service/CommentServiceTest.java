package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1L;
        long writerId = 10L;
        CommentSaveRequest request = new CommentSaveRequest("contents");

        given(userRepository.findById(writerId)).willReturn(Optional.of(new User("temp", "pw", UserRole.USER)));
        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(writerId, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        long writerId = 10L;
        CommentSaveRequest request = new CommentSaveRequest("contents");


        User writer = new User("email@test.com", "encPw", UserRole.USER);
        Todo todo = new Todo("title", "title", "contents", writer);
        Comment saved = new Comment(request.getContents(), writer, todo);


        given(userRepository.findById(writerId)).willReturn(Optional.of(writer));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(saved);


        // when
        CommentSaveResponse result = commentService.saveComment(writerId, todoId, request);

        // then
        assertNotNull(result);
        assertEquals(saved.getContents(), result.getContents());
        assertEquals(writer.getId(), result.getUser().getId());
    }
}
