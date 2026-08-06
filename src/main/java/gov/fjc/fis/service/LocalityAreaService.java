package gov.fjc.fis.service;

import gov.fjc.fis.entity.personnel.LocalityArea;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("fis_LocalityAreaService")
public class LocalityAreaService {

    private final DataManager dataManager;

    public LocalityAreaService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public List<LocalityArea> getCurrentLocalityAreas() {
        return dataManager.load(LocalityArea.class)
                .query("SELECT l FROM fis_LocalityArea l"
                        + " WHERE l.effdt = (SELECT max(e.effdt) FROM fis_LocalityArea e"
                        + " WHERE e.gvtLocalityArea = l.gvtLocalityArea)")
                .list();
    }
}