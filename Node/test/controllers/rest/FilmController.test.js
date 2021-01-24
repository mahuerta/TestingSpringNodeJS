const app = require('../../../src/app')
const supertest = require('supertest');
const createTableIfNotExist = require("../../../src/db/createTable")
const { GenericContainer } = require("testcontainers");
let AWS = require('aws-sdk');

const request = supertest(app);

let dynamodbContainer;

const persistDB = async (request) => {
  const film = { title: 'Watchmen', year : 2009, director: 'Zack Snyder'};
    await request.post('/api/films').send(film).expect(201);
}

beforeAll(async () => {
  dynamodbContainer = await new GenericContainer("amazon/dynamodb-local",
    "1.13.6")
    .withExposedPorts(8000)
    .start().catch((err) => {
      console.log(err)
    });

  AWS.config.update({
    region: process.env.AWS_REGION || 'local',
    endpoint: process.env.AWS_DYNAMO_ENDPOINT || 'http://localhost:8000',
    accessKeyId: "xxxxxx", // No es necesario poner nada aquí
    secretAccessKey: "xxxxxx" // No es necesario poner nada aquí
  });

  await createTableIfNotExist("films");
  await persistDB(request);

});

afterAll(async () => {
  if (!!dynamodbContainer) {
    await dynamodbContainer.stop();
  }
});

const film = { title: 'StarWars La Amenaza fantasma', year: 1999, director: 'George Lucas' };

test('Get films', async () => {


  const response = await request.get('/api/films/').expect(200);
  const [{title, id}] = response.body;

  expect(response.statusCode).toBe(200);
  expect(id).toBe(0);
  expect(title).toBe('Watchmen');



});

test('Get no films', async () => {
  let data = {
    "Items": []
  }


  const response = await request.get('/api/films/')
    .expect('Content-type', /json/)
    .expect(200)
  const [{ title, id }] = response.body;

  expect(response.body).toEqual(expect.arrayContaining(data.Items));
  expect(response.statusCode).toBe(200);
  //Devuelve pelis? hay que mirarlo bien 
  expect(id).toBe(1);
  expect(title).toBe(film.title);
});

test('Create film', async () => {

  const response = await request.post('/api/films/')
    .expect('Content-type', /json/)
    .send(film)
    .expect(201);
  const { title, year, director, id } = response.body;


  expect(id).toEqual((expect.any(Number)));
  expect(title).toBe(film.title);
  expect(year).toBe(film.year);
  expect(director).toBe(film.director);
  expect(response.statusCode).toBe(201);
});