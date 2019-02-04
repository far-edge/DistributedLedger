process.env.NODE_ENV = 'test';

let chai = require('chai');
const assert = chai.assert;
let chaiHttp = require('chai-http');
let server = require('../src/server');

const CONFIG = require('../resources/config.json');

const SecureStateSharing = require('../src/SecureStateSharing');
const secureStateSharing = new SecureStateSharing();
const orionHandler = secureStateSharing.getOrionHandler();
let entity = null;
const TIMEOUT_TEST = 9000;
chai.use(chaiHttp);

function createMockEntity() {
    const rand = Math.floor((Math.random() * 100) + 1);
    entity = {
        "id": "Room" + rand,
        "type": "Room",

        "temperature": {
            "value": 3323,
            "type": "Float"
        },
        "pressure": {
            "value": 720,
            "type": "Integer"
        }
    };
}

async function postEntity() {
    try {
        chai.request(server)
            .post('/v2/entities')
            .set('Content-Type', 'application/json')
            .set('Fiware-Service', 'testService')
            .set('Fiware-ServicePath', '/testSubService')
            .send(entity)
            .end((err, res) => {
                assert.equals(res.status, 201);
                const post = async () => {
                    const entityMasterRes = await orionHandler.getEntity(entity.id, entity.type);
                    assert.equal(entityRes.entity.temperature.value, entityMasterRes.entity.temperature.value);
                    assert.equal(entityRes.entity.pressure.value, entityMasterRes.entity.pressure.value);
                }
                setTimeout(post, CONFIG.timeout + TIMEOUT_TEST);
            });

    } catch (error) {
        console.error(error);
        assert.fail(error);
    }

}

async function putEntireEntity() {
    try {
        entity.temperature.value = 6666;
        entity.pressure.value = 0;
        chai.request(server)
            .put('/v2/entities')
            .set('Content-Type', 'application/json')
            .set('Fiware-Service', 'testService')
            .set('Fiware-ServicePath', '/testSubService')
            .send(entity)
            .end((err, res) => {
                assert.isBelow(res.status, 300);
            });
        const put = async () => {
            const entityMasterRes = await orionHandler.getEntity(entity.id, entity.type);
            assert.equal(entityRes.entity.temperature.value, 6666);
            assert.equal(entityRes.entity.pressure.value, 0);

            assert.equal(entityMasterRes.entity.temperature.value, 6666);
            assert.equal(entityMasterRes.entity.pressure.value, 0);
        }
        setTimeout(put, CONFIG.timeout + TIMEOUT_TEST);
    } catch (error) {
        console.error(error);
        assert.fail(error);
    }
}

async function putPartialEntity() {
    try {
        entity.pressure.value = -1;
        chai.request(server)
            .put('/v2/entities/Room001/attrs/pressure?type=Room')
            .set('Content-Type', 'application/json')
            .set('Fiware-Service', 'testService')
            .set('Fiware-ServicePath', '/testSubService')
            .send(entity)
            .end((err, res) => {
                assert.isBelow(res.status, 300);
            });
        const put = async () => {
            const entityRes = await orionHandler.getEntity(entity.id, entity.type);
            const id = entity.id;
            const entityMasterRes = await orionHandler.getEntity(id, entity.type);
            assert.equal(entityRes.entity.pressure.value, -1);
            assert.equal(entityMasterRes.entity.pressure.value, -1);
        }
        setTimeout(put, CONFIG.timeout + TIMEOUT_TEST);
    } catch (error) {
        console.error(error);
        assert.fail(error);
    }
}


async function deleteEntity() {
    try {
        entity.pressure.value = -1;
        chai.request(server)
            .delete('/v2/entities/Room001/attrs/pressure?type=Room')
            .set('Content-Type', 'application/json')
            .set('Fiware-Service', 'testService')
            .set('Fiware-ServicePath', '/testSubService')
            .send(entity)
            .end((err, res) => {
                assert.isBelow(res.status, 300);
            });
        const put = async () => {
            const entityMasterRes = await orionHandler.getEntity(entity.id, entity.type);
            assert.equal(entityRes.entity.pressure.value, -1);
            assert.equal(entityMasterRes.entity.pressure.value, -1);
        }
        setTimeout(put, CONFIG.timeout + TIMEOUT_TEST);
    } catch (error) {
        console.error(error);
        assert.fail(error);
    }
}


describe('Entities', () => {
    before(() => {
        createMockEntity();
    });

    /*after(async () => {
         try {
             const response = await orionHandler.listEntities();
             if (response.results)
                 for (const res of response.results) {
                     await orionHandler.deleteEntity(res.id, res.type);
                 }
         } catch (error) {
             console.error(error)
         }
     });*/
    describe('/GET Retrieve entities', () => {
        it('it should GET all the entities in OCB', () => {
            chai.request(server)
                .get('/v2/entities')
                .end((err, res) => {
                    assert.equal(res.status, 200);
                    assert.typeOf(res.body, 'array');
                });
        });
    });

    describe('/POST Creation', () => {
        it('it should POST Entity and MASTER TWIN on root API', postEntity);
    });
    describe('/PUT Update', () => {
        it('it should PUT Entity data full entity', putEntireEntity);
        it('it should PUT Entity partial data', putPartialEntity);
    });
    describe('/DELETE Delete', () => {
        it('it should DELETE Entity data', deleteEntity);
    });
});