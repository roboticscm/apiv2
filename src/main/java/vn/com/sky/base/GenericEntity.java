package vn.com.sky.base;

import java.lang.reflect.Field;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import vn.com.sky.annotations.EncryptData;
import vn.com.sky.annotations.EncryptData.Algorithm;
import vn.com.sky.security.SecurityUtil;
import vn.com.sky.util.SDate;

@Data
public class GenericEntity {
    @Transient
    @Value("${suntech.data.encrypt}")
    private boolean encrypt = true;

    @Id
    private Long id;

    private Long createdBy;
    private Long createdDate = SDate.now();
    private Long updatedBy;
    private Long updatedDate;
    private Long deletedBy;
    private Long deletedDate;
    private Integer version = 1;
    private Boolean disabled = false;

    public void updatedBy(Long userId) {
        setUpdatedBy(userId);
        setUpdatedDate(SDate.now());
    }

    public void createdBy(Long userId) {
        setCreatedBy(userId);
    }

    public void deletedBy(Long userId) {
        setDeletedBy(userId);
        setDeletedDate(SDate.now());
    }

    public void cloneParent(Object source) {
        BeanUtils.copyProperties(source, this);
    }

    public GenericEntity clone() {
        try {
            return (GenericEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void encryptProcessing() {
        if (!encrypt) return;

        var clazz = this.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(EncryptData.class)) {
                EncryptData encryptData = field.getAnnotation(EncryptData.class);
                if (encryptData.algorithm() == Algorithm.Flowfish) {
                    try {
                        field.setAccessible(true);
                        String fieldValue = (String) field.get(this);

                        String encryptFieldValue = SecurityUtil.blowfishBase64Encrypt(fieldValue);
                        field.set(this, encryptFieldValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void decryptProcessing() {
        if (!encrypt) return;

        var clazz = this.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(EncryptData.class)) {
                EncryptData encryptData = field.getAnnotation(EncryptData.class);
                if (encryptData.algorithm() == Algorithm.Flowfish) {
                    try {
                        field.setAccessible(true);
                        String fieldValue = (String) field.get(this);
                        field.set(this, SecurityUtil.blowfishBase64Decrypt(fieldValue));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
