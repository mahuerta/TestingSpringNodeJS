const app = require('../../../src/app')
const supertest = require('supertest');
const createTableIfNotExist = require("../../../src/db/createTable")

const { GenericContainer } = require("testcontainers");
const request = supertest(app)
let dynamodbContainer;

beforeAll(async () => {
  dynamodbContainer = await new GenericContainer("amazon/dynamodb-local","1.13.6")
  .withExposedPorts(8000)
  .start();
  await createTableIfNotExist("films");
});



test('Get all films', async () => {

  const response = await request.get('/api/films/')
  .expect('Content-type', /json/)
  .expect(200);
  console.log(response);

  // expect(response.body.author).toBe("Michel")
})