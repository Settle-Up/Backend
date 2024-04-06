package settleup.backend.global.Util;

import org.springframework.stereotype.Component;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;


@Component
public class ServerCryptUtil {

    private static final String ALGORITHM = "AES";
    private static SecretKeySpec secretKey;

    public static void setKey(String myKey) throws CustomException {
        try {
            byte[] key = myKey.getBytes("UTF-8");
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (UnsupportedEncodingException e) {
            throw new CustomException(ErrorCode.ERROR_CREATION_TOKEN_IN_SERVER_B);
        }
    }

    public static String encrypt(String strToEncrypt, String secret) throws CustomException {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | UnsupportedEncodingException e) {
            throw new CustomException(ErrorCode.ERROR_CREATION_TOKEN_IN_SERVER_A);
        }
    }

    public static String decrypt(String strToDecrypt, String secret) throws CustomException {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new CustomException(ErrorCode.ERROR_PARSE_TOKEN_IN_SERVER);
        }
    }
}