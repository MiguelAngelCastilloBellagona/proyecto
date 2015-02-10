package es.ficonlan.web.api.model.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordManager {
	
	public static String hashPassword(String password) {
		try {
			MessageDigest m = MessageDigest.getInstance("SHA-512");

			m.reset();
			m.update(password.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			String hashtext = bigInt.toString(16);

			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static String generatePassword() {
		int passtam = 12;
		char[] elementos={'0','1','2','3','4','5','6','7','8','9' ,'a',
				'b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t',
				'u','v','w','x','y','z'};
		
		char[] conjunto = new char[passtam];
		
		for(int i=0;i<passtam;i++){
			int el = (int)(Math.random()*35);
			conjunto[i] = (char)elementos[el];
		}
		return new String(conjunto);
	}
}
