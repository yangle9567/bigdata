package com.chrtc.utils;

public class IDCardUtil {

    /**
     *
     * @param century  19xx ���� 19��20xx ���� 20
     * @param idCardNo15 ��ת���� 15 λ���֤����
     * @return
     */
    public static String from15to18(int century, String idCardNo15) {

        String centuryStr = "" + century;
        if(century <0 || centuryStr.length() != 2)
            throw new IllegalArgumentException("��������Ч��Ӧ������λ����������");
        if(!(isIdCardNo(idCardNo15) && idCardNo15.length() == 15))
            throw new IllegalArgumentException("�ɵ����֤�Ÿ�ʽ����ȷ��");

        int[] weight = new int[] {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};

        // ͨ������������, ��� 17 Ϊ���º��뱾��.
        String newNoBody = idCardNo15.substring(0, 6) + centuryStr + idCardNo15.substring(6);

        //���������һλУ����

        int checkSum = 0;
        for(int i=0; i< 17; i++) {
            int ai = Integer.parseInt("" + newNoBody.charAt(i)); // λ�� i λ�õ���ֵ
            checkSum = checkSum + ai * weight[i];
        }

        int checkNum = checkSum % 11;
        String checkChar = null;

        switch(checkNum) {
            case 0: checkChar = "1"; break;
            case 1: checkChar = "0"; break;
            case 2: checkChar = "X"; break;
            default: checkChar = "" + (12 - checkNum);
        }

        return newNoBody + checkChar;

    }


    public static String from18to15(String idCardNo18) {

        if(!(isIdCardNo(idCardNo18) && idCardNo18.length() == 18))
            throw new IllegalArgumentException("���֤�Ų�����ʽ����ȷ��");

        return idCardNo18.substring(0, 6) + idCardNo18.substring(8, 17);
    }

    /**
     * �жϸ������ַ����ǲ��Ƿ������֤�ŵ�Ҫ��
     * @param str
     * @return
     */
    public static boolean isIdCardNo(String str) {

        if(str == null)
            return false;

        int len = str.length();
        if(len != 15 && len != 18)
            return false;

        for(int i=0; i<len; i++) {
            try {
                Integer.parseInt("" + str.charAt(i));
            }
            catch(NumberFormatException e) {
                return false;
            }
        }

        return true;
    }


    public static void main(String[] args) {

        System.out.println(from15to18(19, "330521820721052"));
        System.out.println(from18to15("330521198207210526"));
    }

}