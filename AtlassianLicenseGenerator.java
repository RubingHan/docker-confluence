import java.util.Arrays;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

/**
 * @author Shauway <shauway@qq.com>
 */
public class AtlassianLicenseGenerator {
    public static final String CONFLUENCE_SERVER_LICENSE_DESC = "Confluence (Server)";
    public static final String CONFLUENCE_DATA_CENTER_LICENSE_DESC = "Confluence (Data Center)";
    public static final String JIRA_SOFTWARE_SERVER_LICENSE_DESC = "Jira Software (Server)";
    public static final String JIRA_SOFTWARE_DATA_CENTER_LICENSE_DESC = "Jira Software (Data Center)";
    public static final String BITBUCKET_SERVER_LICENSE_DESC = "Bitbucket (Server)";
    public static final String BITBUCKET_DATA_CENTER_LICENSE_DESC = "Bitbucket (Data Center)";
    public static final String BAMBOO_SERVER_LICENSE_DESC = "Bamboo (Server) Unlimited Remote Agents";
    public static final String CRUCIBLE_SERVER_LICENSE_DESC = "Crucible (Server)";
    public static final String FISH_EYE_SERVER_LICENSE_DESC = "FishEye (Server)";
    public static final String CROWD_SERVER_LICENSE_DESC = "Crowd(Server)";
    static String COMMON_LICENSE_CONTENT = "Description=$$licenseDesc$$\n" +
            "ServerID=$$serverid$$\n" +
            "Organisation=$$org$$\n" +
            "ContactEMail=$$email$$\n" +
            "LicenseTypeName=COMMERCIAL\n" +
            "NumberOfUsers=-1\n" +
            "Subscription=true\n" +
            "Evaluation=false\n" +
            "PurchaseDate=2017-03-14\n" +
            "CreationDate=2017-03-14\n" +
            "LicenseExpiryDate=$$license_expiry_date$$\n" +
            "MaintenanceExpiryDate=$$maintenance_expiry_date$$\n" +
            "LicenseID=LIDSEN-L9418768\n" +
            "SEN=SEN-L9418768\n" +
            "licenseVersion=2\n";
    static String CONFLUENCE_SPECIAL_LICENSE_CONTENT = "conf.LicenseEdition=ENTERPRISE\n" +
            "conf.DataCenter=$$data_center$$\n" +
            "conf.active=true\n" +
            "conf.NumberOfUsers=-1\n" +
            "conf.Starter=false\n" +
            "conf.LicenseTypeName=COMMERCIAL";
    static String JIRA_SPECIAL_LICENSE_CONTENT = "greenhopper.LicenseEdition=ENTERPRISE\n" +
            "jira.NumberOfUsers=-1\n" +
            "jira.product.jira-software.active=true\n" +
            "jira.product.jira-software.DataCenter=false\n" +
            "jira.LicenseEdition=ENTERPRISE\n" +
            "greenhopper.LicenseTypeName=COMMERCIAL\n" +
            "jira.product.jira-software.NumberOfUsers=-1\n" +
            "jira.DataCenter=$$data_center$$\n" +
            "jira.product.jira-software.Starter=false\n" +
            "greenhopper.enterprise=true\n" +
            "jira.active=true\n" +
            "jira.LicenseTypeName=COMMERCIAL\n" +
            "greenhopper.active=true";
    static String BITBUCKET_SPECIAL_LICENSE_CONTENT = "stash.LicenseTypeName=COMMERCIAL\n" +
            "stash.DataCenter=$$data_center$$\n" +
            "stash.active=true\n" +
            "stash.NumberOfUsers=-1\n" +
            "stash.Starter=false";
    static String BAMBOO_SPECIAL_LICENSE_CONTENT = "bamboo.LicenseEdition=ENTERPRISE\n" +
            "bamboo.NumberOfBambooLocalAgents=-1\n" +
            "bamboo.active=true\n" +
            "bamboo.NumberOfBambooRemoteAgents=-1\n" +
            "bamboo.NumberOfBambooPlans=-1\n" +
            "bamboo.LicenseTypeName=COMMERCIAL";
    static String CRUCIBLE_SPECIAL_LICENSE_CONTENT = "crucible.LicenseTypeName=COMMERCIAL\n" +
            "crucible.Starter=false\n" +
            "crucible.NumberOfUsers=-1\n" +
            "crucible.active=true";
    static String FISH_SPECIAL_LICENSE_CONTENT = "fisheye.active=true\n" +
            "fisheye.Starter=false\n" +
            "fisheye.LicenseTypeName=COMMERCIAL\n" +
            "fisheye.NumberOfUsers=-1";
    static String CROWD_SPECIAL_LICENSE_CONTENT = "crowd.active=true\n" +
            "crowd.Starter=false\n" +
            "crowd.LicenseTypeName=COMMERCIAL\n" +
            "crowd.NumberOfUsers=-1";

    static String LINE_SEPARATOR = System.getProperty("line.separator");
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        System.out.print("Select generate(0) or decode(1) license:");
        int genOrDe = scanner.nextInt();
        if (genOrDe == 1) {
            System.out.print(">> Paste your license:");
            String licenseText = scanner.next();
            decode(licenseText);
            return;
        }
        StringBuffer prompt = new StringBuffer();
        prompt.append("Select license type:").append(LINE_SEPARATOR);
        prompt.append("\t11: ").append(CONFLUENCE_SERVER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t12: ").append(CONFLUENCE_DATA_CENTER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t21: ").append(JIRA_SOFTWARE_SERVER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t22: ").append(JIRA_SOFTWARE_DATA_CENTER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t31: ").append(BITBUCKET_SERVER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t32: ").append(BITBUCKET_DATA_CENTER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t4: ").append(BAMBOO_SERVER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t5: ").append(CRUCIBLE_SERVER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t6: ").append(FISH_EYE_SERVER_LICENSE_DESC).append(LINE_SEPARATOR);
        prompt.append("\t7: ").append(CROWD_SERVER_LICENSE_DESC).append(LINE_SEPARATOR);
        System.out.print(prompt);
        System.out.print(">> Input selected license type code: ");
        int licenseTypeCode = scanner.nextInt();
        switch (licenseTypeCode) {
            case 11:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", CONFLUENCE_SERVER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "unlimited")
                        .replace("$$maintenance_expiry_date$$", "253402257599855");
                CONFLUENCE_SPECIAL_LICENSE_CONTENT = CONFLUENCE_SPECIAL_LICENSE_CONTENT.replace("$$data_center$$", "false");
                break;
            case 12:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", CONFLUENCE_DATA_CENTER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "253402257599855")
                        .replace("$$maintenance_expiry_date$$", "253402257599855");
                CONFLUENCE_SPECIAL_LICENSE_CONTENT = CONFLUENCE_SPECIAL_LICENSE_CONTENT.replace("$$data_center$$", "true");
                break;
            case 21:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", JIRA_SOFTWARE_SERVER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "unlimited")
                        .replace("$$maintenance_expiry_date$$", "253402257599855");
                JIRA_SPECIAL_LICENSE_CONTENT = JIRA_SPECIAL_LICENSE_CONTENT.replace("$$data_center$$", "false");
                break;
            case 22:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", JIRA_SOFTWARE_DATA_CENTER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "253402257599855")
                        .replace("$$maintenance_expiry_date$$", "253402257599855");
                JIRA_SPECIAL_LICENSE_CONTENT = JIRA_SPECIAL_LICENSE_CONTENT.replace("$$data_center$$", "true");
                break;
            case 31:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", BITBUCKET_SERVER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "unlimited")
                        .replace("$$maintenance_expiry_date$$", "253402257599855");
                BITBUCKET_SPECIAL_LICENSE_CONTENT = BITBUCKET_SPECIAL_LICENSE_CONTENT.replace("$$data_center$$", "false");
                break;
            case 32:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", BITBUCKET_DATA_CENTER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "253402257599855")
                        .replace("$$maintenance_expiry_date$$", "253402257599855");
                BITBUCKET_SPECIAL_LICENSE_CONTENT = BITBUCKET_SPECIAL_LICENSE_CONTENT.replace("$$data_center$$", "true");
                break;
            case 4:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", BAMBOO_SERVER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "unlimited")
                        .replace("$$maintenance_expiry_date$$", "4102401599852");
                break;
            case 5:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", CRUCIBLE_SERVER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "unlimited")
                        .replace("$$maintenance_expiry_date$$", "4102401599852");
                break;
            case 6:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", FISH_EYE_SERVER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "unlimited")
                        .replace("$$maintenance_expiry_date$$", "4102401599852");
                break;
            case 7:
                COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$licenseDesc$$", CROWD_SERVER_LICENSE_DESC)
                        .replace("$$license_expiry_date$$", "unlimited")
                        .replace("$$maintenance_expiry_date$$", "4102401599852");
                break;
            default:
                System.out.println("Invalid license type code!");
                System.exit(1);
        }
        System.out.print(">> Input your server id: ");
        String serverId = scanner.next();
        System.out.print(">> Input your organisation name: ");
        String organisation = scanner.next();
        System.out.print(">> Input your email address: ");
        String email = scanner.next();
        COMMON_LICENSE_CONTENT = COMMON_LICENSE_CONTENT.replace("$$serverid$$", serverId)
                .replace("$$org$$", organisation).replace("$$email$$", email);
        String licenseContent = null;
        switch (licenseTypeCode) {
            case 11:
            case 12:
                licenseContent = COMMON_LICENSE_CONTENT + CONFLUENCE_SPECIAL_LICENSE_CONTENT;
                break;
            case 21:
            case 22:
                licenseContent = COMMON_LICENSE_CONTENT + JIRA_SPECIAL_LICENSE_CONTENT;
                break;
            case 31:
            case 32:
                licenseContent = COMMON_LICENSE_CONTENT + BITBUCKET_SPECIAL_LICENSE_CONTENT;
                break;
            case 4:
                licenseContent = COMMON_LICENSE_CONTENT + BAMBOO_SPECIAL_LICENSE_CONTENT;
                break;
            case 5:
                licenseContent = COMMON_LICENSE_CONTENT + CRUCIBLE_SPECIAL_LICENSE_CONTENT;
                break;
            case 6:
                licenseContent = COMMON_LICENSE_CONTENT + FISH_SPECIAL_LICENSE_CONTENT;
                break;
            case 7:
                licenseContent = COMMON_LICENSE_CONTENT + CROWD_SPECIAL_LICENSE_CONTENT;
                break;
        }
        System.out.println();
        System.out.println("Your license content. Enjoy it!");
        System.out.println();
        System.out.println(getLicense(licenseContent));
        System.out.println();
        System.out.println(licenseContent);
    }

    static PublicKey publicKey;
    static PrivateKey privateKey;
    static byte[] licenseTextPrefix = {13, 14, 12, 10, 15};

    static {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            /* 原作者的Key */
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getUrlDecoder().decode("MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1_U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq_xfW6MPbLm1Vs14E7gB00b_JmYLdrmVClpJ-f6AR7ECLCT7up1_63xhv4O1fnxqimFQ8E-4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC_BYHPUCgYEA9-GghdabPd7LvKtcNrhXuXmUr7v6OuqC-VdMCz0HgmdRWVeOutRZT-ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN_C_ohNWLx-2J6ASQ7zKTxvqhRkImog9_hWuWfBpKLZl6Ae1UlZAFMO_7PSSoDgYUAAoGBAOshUqTDMJgJhrrooXl9ajUjDyunW8FSX1IjOOyNRwd0TEwtzfZzzAzUsGm4bPYjIHQpe1ovONVVUpEzYJGJMxVXbbBHQYMbevdvSUdq90LLWXhgwwlXRAwqPq9S0YZP7r9uisPruk59LVj-D-L_GVacH01LlWkm74ya1CusMxDc")));
            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getUrlDecoder().decode("MIIBTAIBADCCASwGByqGSM44BAEwggEfAoGBAP1_U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq_xfW6MPbLm1Vs14E7gB00b_JmYLdrmVClpJ-f6AR7ECLCT7up1_63xhv4O1fnxqimFQ8E-4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC_BYHPUCgYEA9-GghdabPd7LvKtcNrhXuXmUr7v6OuqC-VdMCz0HgmdRWVeOutRZT-ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN_C_ohNWLx-2J6ASQ7zKTxvqhRkImog9_hWuWfBpKLZl6Ae1UlZAFMO_7PSSoEFwIVAIPdS-RMIsqurIg1ONM3UjobnZiz")));
            /* confluence5.1-crack中的key
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode("MIHwMIGoBgcqhkjOOAQBMIGcAkEA/KaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII864WF64B81uRpH5t9jQTxeEu0ImbzRMqzVDZkVG9xD7nN1kuFwIVAJYu3cw2nLqOuyYO5rahJtk0bjjFAkBnhHGyepz0TukaScUUfbGpqvJE8FpDTWSGkx0tFCcbnjUDC3H9c9oXkGmzLik1Yw4cIGI1TQ2iCmxBblC+eUykA0MAAkBrKJN92XEUFWggagAhhhNtFVc/Nh/JTnB3xsQ5azfHq7UcFtPEq0ohc3vGZ7OGEQS7Ym08DB6B1DtD93CwaNdX")));
            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode("MIHGAgEAMIGoBgcqhkjOOAQBMIGcAkEA/KaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII864WF64B81uRpH5t9jQTxeEu0ImbzRMqzVDZkVG9xD7nN1kuFwIVAJYu3cw2nLqOuyYO5rahJtk0bjjFAkBnhHGyepz0TukaScUUfbGpqvJE8FpDTWSGkx0tFCcbnjUDC3H9c9oXkGmzLik1Yw4cIGI1TQ2iCmxBblC+eUykBBYCFBJm/ZWcfH/gZJU3FHsqy/8aD68B")));
            */
        } catch (Exception e) {
        }
    }

    static String getLicense(String licenseContent) throws Exception {
        byte[] licenseTextBytes = licenseContent.getBytes("UTF-8"); // 未压缩

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(baos2, deflater);
        deflaterOutputStream.write(licenseTextBytes);
        deflaterOutputStream.close();
        byte[] licenseTextBytesZiped = baos2.toByteArray(); // 已压缩

        Signature signature = Signature.getInstance("SHA1withDSA");
        signature.initSign(privateKey);
        signature.update(licenseTextPrefix);
        signature.update(licenseTextBytesZiped);
        byte[] sign = signature.sign();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(licenseTextBytesZiped.length + licenseTextPrefix.length);
        dos.write(licenseTextPrefix);
        dos.write(licenseTextBytesZiped);
        dos.write(sign);

        dos.close();
        byte[] licenseContentBytes = baos.toByteArray();
        String licenseContentBase64 = Base64.getEncoder().encodeToString(licenseContentBytes);
        String licenseContentWithVersion = licenseContentBase64 + "X02";
        return licenseContentWithVersion + Integer.toString(licenseContentBase64.length(), 31);
    }

    static void decode(String licenseText) throws Exception {
        int len = licenseText.length();
        String part1 = licenseText.substring(0, len-5);
        System.out.println(part1);
        byte[] part1bin = Base64.getDecoder().decode(part1);
        ByteArrayInputStream bais = new ByteArrayInputStream(part1bin);
        DataInputStream bis = new DataInputStream(bais);
        int p2p3len = bis.readInt();

        byte[] part1p3 = Arrays.copyOfRange(part1bin, 9, p2p3len - 5);
        System.out.println("License text len" + part1p3.length);

        byte[] part1p3unzip = decompress(part1p3);

        String str = new String(part1p3unzip, "UTF-8");
        System.out.println(str);
    }

    public static byte[] decompress(byte[] data) {
        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            int iter = 0;
            while(!decompresser.finished() && iter++ < 10) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }
}
