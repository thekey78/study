package pe.kr.thekey78.messenger.utils;

import pe.kr.thekey78.messenger.MessageException;
import pe.kr.thekey78.messenger.MessageExtension;
import pe.kr.thekey78.messenger.annotation.CharEncoding;
import pe.kr.thekey78.messenger.annotation.Extension;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.enumeration.Align;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class VoUtils {
    public static byte[] toMessageBytes(String src, Length a_length) throws MessageException {
        return toMessageBytes(src, a_length, null);
    }

    public static byte[] toMessageBytes(String src, Length a_length, CharEncoding charset) throws MessageException {
        if (a_length != null && a_length.value() > 0) {
            int length = a_length.value();
            byte[] result = new byte[length];
            Arrays.fill(result, a_length.pad());

            byte[] deco;
            if(charset == null)
                deco = src.getBytes();
            else {
                try {
                    deco = src.getBytes(charset.value());
                } catch (UnsupportedEncodingException exception) {
                    throw new MessageException(exception);
                }
            }

            pad(a_length, deco, result);
            return result;
        }
        else if(charset == null) {
            return src.getBytes();
        }
        else {
            try {
                return src.getBytes(charset.value());
            } catch (UnsupportedEncodingException exception) {
                throw new MessageException(exception);
            }
        }
    }

    public static void pad(Length aLength, byte[] deco, byte[] result) {
        int length = aLength.value();

        if(aLength.align() == Align.LEFT) {
            System.arraycopy(deco, 0, result, 0, Math.min(length, deco.length));
        }
        else {
            System.arraycopy(deco, 0, result, Math.max(result.length - deco.length, 0), Math.min(length, deco.length));
        }
    }

    public static byte[] unPad(byte[] bytes, Length aLength) {
        if (aLength.align() == Align.RIGHT) {
            int i = 0;
            for (; i < bytes.length; i++) {
                if(bytes[i] != aLength.pad())
                    break;
            }

            return Arrays.copyOfRange(bytes, i, bytes.length);
        }
        else {
            int i = bytes.length-1;
            for (; i > 0; i--) {
                if(bytes[i] != aLength.pad())
                    break;
            }

            return Arrays.copyOfRange(bytes, 0, i+1);
        }
    }

    public static String doExtension(String str, Extension extension) {
        String result = str;
        if(extension != null) {
            //확장필드 처리
            InitialContext initCtx;
            try {
                initCtx = new InitialContext();
                for (String name : extension.value()) {
                    Object object = initCtx.lookup(name);
                    if(object instanceof MessageExtension) {
                        result = ((MessageExtension) object).doExtension(str);
                    }
                    else
                        throw new MessageException(object.getClass().getName() + " is must implement MessageExtension.");
                }
            } catch (NamingException e) {
                throw new MessageException(e);
            }
        }
        return result;
    }
}
