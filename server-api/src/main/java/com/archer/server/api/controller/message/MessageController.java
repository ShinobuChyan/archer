package com.archer.server.api.controller.message;

import com.archer.server.core.model.MessageDTO;
import com.archer.server.core.service.message.MessageService;
import com.archer.server.common.model.CommonResult;
import com.archer.server.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Shinobu
 * @since 2018/11/29
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 生成消息
     */
    @PostMapping("/produce")
    public CommonResult produce(@RequestBody @Validated MessageDTO dto) {
        Assert.notEmpty(dto.getIntervalList());
        try {
            var id = messageService.insert(dto.getRefId(), dto.getExtraInfo(), dto.getSource(), dto.getTopic(), dto.getTag(),
                    dto.getUrl(), dto.getMethod(), dto.getContentType(), dto.getHeaders(), dto.getContent(),
                    dto.getIntervalList(), dto.getFirstTime(), dto.getStoppedKeyWords());
            return CommonResult.commonSuccessResult("生成消息成功", id);
        } catch (Exception e) {
            LOGGER.error("生成消息失败", e);
            return CommonResult.commonFailedResult("生成失败", null);
        }
    }

    /**
     * 变更消息状态 - 主动结束
     */
    @GetMapping("/status/changeToStopped")
    public CommonResult changeStatusToStopped(String messageId) {
        Assert.notNull(messageId);
        messageService.changeStatusToStopped(messageId);
        return CommonResult.commonSuccessResult("消息已置为“主动结束”状态", null);
    }

    /**
     * 变更消息状态 - 响应结束
     */
    @GetMapping("/status/changeToResponded")
    public CommonResult changeStatusToResponded(String messageId) {
        Assert.notNull(messageId);
        messageService.changeStatusToResponded(messageId);
        return CommonResult.commonSuccessResult("消息已置为“响应结束”状态", null);
    }

}
