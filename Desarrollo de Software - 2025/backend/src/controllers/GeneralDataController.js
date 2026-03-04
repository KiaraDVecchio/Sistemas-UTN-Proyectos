
export class GeneralDataController {
    constructor(generalDataService) {
        this.generalDataService = generalDataService;
    }

    async getLocalidadesByPais(req, res) {
        const ciudades = await this.generalDataService.getLocalidadesByPais(req.params.paisId)
        res.send({ data: ciudades })
    }
}
