//package settleup.backend.domain.user.service.Impl;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import settleup.backend.domain.user.entity.dto.UserInfoDto;
//import settleup.backend.domain.user.service.EmailSenderService;
//import settleup.backend.global.config.EmailSendConfig;
//import settleup.backend.global.exception.CustomException;
//
//import java.util.Map;
//
//@Service
//@Transactional
//public class EmailSenderServiceImpl implements EmailSenderService {
//    private EmailSendConfig emailConfig;
//    @Override
//    public String sendEmailToNewGroupUser(Map<String, String> userInfoList) throws CustomException {
//        if (!userInfoList.isEmpty()) {
//            for (Map<String, String> userInfo : userInfoList) {
//                UserInfoDto userInfoDto = new UserInfoDto();
//                userInfoDto.setUserName(userInfo.get(0));
//                userInfoDto.setUserEmail(userInfo.get(1));
//
//
//            }
//        }
//        return  null;
//    }
//}
