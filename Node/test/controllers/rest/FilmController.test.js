const app = require('../../../src/app')
const supertest = require('supertest');
let AWS = require('aws-sdk');

const { GenericContainer } = require("testcontainers");
const createTableIfNotExist = require("../../../src/db/createTable")

let request =  supertest(app);
let dynamodbContainer;

jest.setTimeout(20000);

beforeAll(async () => {
  dynamodbContainer = await new GenericContainer("amazon/dynamodb-local:1.13.6")
  .withExposedPorts(8000)
  .start().catch((err) => {
    console.log(err)
  });

  AWS.config.update({
    region: 'local',
    endpoint: 'http://localhost:'+dynamodbContainer.getMappedPort(8000),
    accessKeyId: "xxxxxx", // No es necesario poner nada aquí
    secretAccessKey: "xxxxxx" // No es necesario poner nada aquí
  });

  await createTableIfNotExist("films");

});

afterAll(async () => {
  await dynamodbContainer.stop();
});

test('Get films', async () => {
  let film = {
    "titulo": "TITULO"
  };

  const responseCreation = await request.post('/api/films/')
  .expect('Content-type', /json/)
  .send(film)
  .expect(201)

  const response = await request.get('/api/films/')
  .expect('Content-type', /json/)
  .expect(200)

  expect(response.body).toContainEqual(responseCreation.body);

  // Como no tenemos borrado, puede afectar el orden, por eso se utiliza mayor o igual
  expect(response.body.length).toBeGreaterThanOrEqual(1)

})

test('Get no films', async () => {
  let data = {
    "Items": []
  }

  const response = await request.get('/api/films/')
  .expect('Content-type', /json/)
  .expect(200)

  expect(response.body).toEqual(expect.arrayContaining(data.Items));

})

test('Create film', async () => {
  let film = {
    "titulo": "TITULO"
  };

  const response = await request.post('/api/films/')
  .expect('Content-type', /json/)
  .send(film)
  .expect(201)

  expect(response.body.id).toEqual((expect.any(Number)));

})