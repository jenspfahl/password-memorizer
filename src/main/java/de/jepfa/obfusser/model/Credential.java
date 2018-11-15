package de.jepfa.obfusser.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.Nullable;

/**
 * A credential can be a certain password or another secret to be obfuscated.
 *
 * @author Jens Pfahl
 */
@Entity(indices =
        {@Index("template_id"), @Index(value = {"group_id"})},
        foreignKeys =
        {@ForeignKey(entity = Template.class,
                parentColumns = "id",
                childColumns = "template_id",
                onDelete = ForeignKey.SET_NULL),
        @ForeignKey(entity = Group.class,
                parentColumns = "id",
                childColumns = "group_id",
                onDelete = ForeignKey.SET_NULL)}
                )
public class Credential extends SecurePatternHolder {

    public static final String ATTRIB_TEMPLATE_ID = "template_id";

    @Nullable
    @ColumnInfo(name = "template_id")
    private Integer templateId;

    @Nullable
    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(@Nullable Integer templateId) {
        this.templateId = templateId;
    }

    public void copyFrom(Template template) {
        super.copyFrom(template);
        setTemplateId(template.getId());
    }

}
