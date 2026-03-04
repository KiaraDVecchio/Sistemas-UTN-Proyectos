
export class GeneralDataService {
    constructor(repo) {
        this.repo = repo;
    }

    async getLocalidadesByPais(pais) {
        return await this.repo.getLocalidadesByPais(pais)
    }

}