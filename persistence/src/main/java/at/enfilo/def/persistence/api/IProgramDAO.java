package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.IdDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.util.UnsupportedMappingException;

import java.util.List;

/**
 * Created by mase on 22.08.2016.
 */
public interface IProgramDAO extends IGenericDAO<Program, String> {

    /**
     * Returns information in DTO form about the requested {@see Program} by given {@see Program} (pId).
     *
     * @param pId {@see Program} (id) that will be used as a data source.
     * @return {@see ProgramDTO} object that contains information about requested {@see Program}.
     * @throws PersistenceException
     * @throws UnsupportedMappingException
     */
    ProgramDTO getProgramInfo(String pId)
    throws PersistenceException, UnsupportedMappingException;

    /**
     * Returns a list of all {@see Job} ids that are associated to a given {@see Program} (pId).
     *
     * @param pId {@see Program} (id) that will be used as a source of {@see Program} - {@see Job} associations.
     * @return a list of all {@see Job} ids that are associated to a given {@see Program} (pId).
     * @throws PersistenceException
     */
    List<IdDTO> getAllJobIds(String pId)
    throws PersistenceException;
}
