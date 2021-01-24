const app = require('../../../src/app')
const supertest = require('supertest')
const request = supertest(app);

const AWS = require('aws-sdk');
jest.mock('aws-sdk');


const mockedData = {
  Items: [
    {
      id: 0,
      title: 'StarWars La Amenaza Fantasma',
      year: 1999,
      director: 'George Lucas'
    },
    {
      id: 1,
      title: 'The Avengers',
      year: 2012,
      director: 'Joss Whedon'
    }
  ]
}

beforeAll(() => {

  const put = (params, cb) => {
    const error = Object.keys(params.Item).length <= 1 ? new Error('Error at mocked AWS module') : null;
    cb(error, params.Item);
  };

  const scan = (params, cb) => {
    cb(null, mockedData);
  };

  AWS.DynamoDB.DocumentClient = jest.fn().mockReturnValue({ put, scan });
});


afterAll(() => {
  AWS.DynamoDB.DocumentClient.mockReset()
});

const getMockedFilm = (number) => mockedData.Items[number - 1];

test('Get films', async () => {

  const response = await request.get('/api/films/').expect(200);
  const [{ title: title1 }, { title: title2 }] = response.body;

  expect(response.statusCode).toBe(200);
  expect(title1).toBe(getMockedFilm(1).title);
  expect(title2).toBe(getMockedFilm(2).title);

});

//Este test para saber si hay 0?
test('Get no films', async () => {
  let data = {
    "Items": []
  }

  const response = await request.get('/api/films/').expect(200)

  expect(response.body).toEqual(expect.arrayContaining(data.Items));

});

test('Create film', async () => {
  const film = { title: 'StarWars La Amenaza fantasma', year: 1999, director: 'George Lucas' };


  const response = await request.post('/api/films/').send(film).expect(201);
  const { title, year, director, id } = response.body;


  expect(id).toBe(0);

  expect(title).toBe(film.title);
  expect(year).toBe(film.year);
  expect(director).toBe(film.director);
  expect(response.statusCode).toBe(201);
});


test('Create a new film fails when a correct film is not provided', async () => {

  const response = await request.post('/api/films').send(null).expect(400);

  expect(response.statusCode).toBe(400);
  expect(response.error).not.toBeNull();
});