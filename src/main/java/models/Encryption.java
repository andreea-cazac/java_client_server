package models;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class Encryption {
private KeyPairGenerator keyPairGenerator;
private KeyPair pair;
private PrivateKey privateKey;
private PublicKey publicKey;
private KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
private SecretKey aesKey = keyGenerator.generateKey();
private String message;
public Encryption() throws NoSuchAlgorithmException {
    keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(1024);
    pair = keyPairGenerator.genKeyPair();
    privateKey = pair.getPrivate();
    publicKey = pair.getPublic();

}
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SecretKey getAesKey() {
        return aesKey;
    }

    public String encryptPublicKey(PublicKey publicKey) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher =  Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE , publicKey);
        byte[] encryptedAesKeySym = cipher.doFinal(aesKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedAesKeySym);
    }

    public SecretKey decryptAESKey(String toBeDecrypted) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher =  Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE , privateKey);
        byte[] toDecrypt = Base64.getDecoder().decode(toBeDecrypted);
        byte[] decryptedAesKeyBytes = cipher.doFinal(toDecrypt);
        SecretKey decryptedAesKey = new SecretKeySpec(decryptedAesKeyBytes, "AES");
        return decryptedAesKey;
    }


}
