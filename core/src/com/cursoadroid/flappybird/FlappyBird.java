package com.cursoadroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sun.media.jfxmedia.MediaPlayer;

import java.awt.Font;
import java.util.Random;

import sun.rmi.runtime.Log;

public class FlappyBird extends ApplicationAdapter {

	Preferences prefs;
	Music baterAsas;

	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoTopo;
	private Texture canoBaixo;
	private Texture gameOver;
	private Random randomico;
	private BitmapFont fonteBranca;
	private BitmapFont fonteAmarela;
	private BitmapFont mensagem;
	private Circle passaroCirculo;
	private Rectangle retanguloCanoTopo;
	private Rectangle retanguloCanoBaixo;
//	private ShapeRenderer shape;

	//Atributos de configuração

	private float larguraDispositivo;
	private float alturaDispositivo;
	private int estadoDoJogo = 0; // 0 -> Não Iniciado, 1 -> Iniciado e 2 -> GameOver
	private int pontuacao = 0;
	private int melhorPontuacao = 0;

	private float variacao = 0;
	private float velocidadeQueda = 0;
	private float posicaoInicialVertical;
	private float posicaoMovimentoCanoHorizontal;
	private float espacoEntreCanos;
	private float deltaTime;
	private float alturaGeradaRandomicamente;
	private boolean marcouPonto = false;

	//Camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 768;
	private final float VIRTUAL_HEIGTH = 1024;


	@Override
	public void create () {

		prefs = Gdx.app.getPreferences("FlappyBird");
		melhorPontuacao = getMelhorPontuacao();

		baterAsas = Gdx.audio.newMusic(Gdx.files.internal("bater_de_asas.mp3"));
		//baterAsas.setVolume(1ç.0f);

		batch = new SpriteBatch();
		randomico = new Random();
		passaroCirculo = new Circle();
//		retanguloCanoTopo = new Rectangle();
//		retanguloCanoBaixo = new Rectangle();
//		shape = new ShapeRenderer();

		fonteBranca = new BitmapFont();
		fonteBranca.setColor(Color.WHITE);
		fonteBranca.getData().setScale(3);

		fonteAmarela = new BitmapFont();
		fonteAmarela.setColor(Color.YELLOW);
		fonteAmarela.getData().setScale(3);

		mensagem = new BitmapFont();
		mensagem.setColor(Color.WHITE);
		mensagem.getData().setScale(3);

		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");

		canoTopo = new Texture("cano_topo.png");
		canoBaixo = new Texture("cano_baixo.png");
		gameOver = new Texture("game_over.png");

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2,VIRTUAL_HEIGTH/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH,VIRTUAL_HEIGTH, camera);

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGTH;

		posicaoInicialVertical = alturaDispositivo / 2;
		posicaoMovimentoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 300;
	}

	@Override
	public void render () {

		camera.update();

		//Limpando frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		deltaTime = Gdx.graphics.getDeltaTime();

		variacao += deltaTime * 10;

		if(variacao > 2){
			variacao = 0;
		}

		if( estadoDoJogo == 0){

			if(Gdx.input.justTouched()){
				estadoDoJogo = 1;
				melhorPontuacao = getMelhorPontuacao();
			}

		}else{

			velocidadeQueda++;

			if (posicaoInicialVertical > 0 || velocidadeQueda < 0) {
				posicaoInicialVertical = posicaoInicialVertical - velocidadeQueda;
			}

			if(estadoDoJogo == 1) {

				posicaoMovimentoCanoHorizontal -= deltaTime * 200;

				if (Gdx.input.justTouched()) {
					velocidadeQueda = -15;
					baterAsas.play();
				}

				//Verifica se o cano saiu inteiramente da tela
				if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth()) {
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
					alturaGeradaRandomicamente = randomico.nextInt(400) - 200;
					marcouPonto = false;
				}

				//Incrementa Pontuação
				if (posicaoMovimentoCanoHorizontal < 120) {
					if (!marcouPonto) {
						pontuacao++;
						marcouPonto = true;
					}
				}
			}else{

				//Tela de GameOver
				if (Gdx.input.justTouched()) {

					estadoDoJogo = 0;
					pontuacao = 0;
					velocidadeQueda = 0;
					posicaoInicialVertical = alturaDispositivo/2;
					posicaoMovimentoCanoHorizontal = larguraDispositivo;

					melhorPontuacao = getMelhorPontuacao();
				}

			}
		}

		//Configurar daods de projeção da câmera
		batch.setProjectionMatrix(camera.combined);


		batch.begin();

		batch.draw(fundo,0,0,larguraDispositivo,alturaDispositivo);
		batch.draw(canoTopo,posicaoMovimentoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaGeradaRandomicamente);
		batch.draw(canoBaixo,posicaoMovimentoCanoHorizontal,alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaGeradaRandomicamente);
		batch.draw(passaros[(int)variacao],120,posicaoInicialVertical);
		fonteAmarela.draw(batch,"Melhor Pontuação: "+String.valueOf(melhorPontuacao),10,alturaDispositivo - 20);
		fonteBranca.draw(batch,"Pontuação Atual: "+String.valueOf(pontuacao),10,alturaDispositivo - 70);


		if(estadoDoJogo == 2){

			batch.draw(
					gameOver,
					larguraDispositivo/2 - gameOver.getWidth()/2,
					alturaDispositivo/2
			);

			mensagem.draw(
					batch,
					"Toque para reiniciar!",
					larguraDispositivo/2 - 200,
					alturaDispositivo/2 - gameOver.getHeight()/2
			);
		}

		batch.end();

		passaroCirculo.set(
				120 + passaros[0].getWidth()/2,
				posicaoInicialVertical + passaros[0].getHeight()/2,passaros[0].getWidth()/2
		);

		retanguloCanoBaixo = new Rectangle(
				posicaoMovimentoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaGeradaRandomicamente,
				canoBaixo.getWidth(),
				canoBaixo.getHeight()
		);

		retanguloCanoTopo = new Rectangle(
				posicaoMovimentoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaGeradaRandomicamente,
				canoTopo.getWidth(),
				canoTopo.getHeight()
		);

		//Desenhar formas
//		shape.begin(ShapeRenderer.ShapeType.Filled);
//		shape.circle(passaroCirculo.x, passaroCirculo.y,passaroCirculo.radius);
//		shape.rect(retanguloCanoBaixo.x,retanguloCanoBaixo.y,retanguloCanoBaixo.width,retanguloCanoBaixo.height);
//		shape.rect(retanguloCanoTopo.x,retanguloCanoTopo.y,retanguloCanoTopo.width,retanguloCanoTopo.height);
//		shape.setColor(Color.RED);
//		shape.end();

		//Teste de colisão
		if(
			Intersector.overlaps(passaroCirculo,retanguloCanoBaixo) ||
			Intersector.overlaps(passaroCirculo,retanguloCanoTopo) ||
			posicaoInicialVertical <= 0 ||
			posicaoInicialVertical >= alturaDispositivo){

				atualizarPontuacao(pontuacao);

				estadoDoJogo = 2;
		}
	}

	@Override
	public void resize (int width, int heigth) {
		viewport.update(width,heigth);
	}

	@Override
	public void dispose () {

	}

	public int getMelhorPontuacao(){
		return prefs.getInteger("melhorPontuacao", 0);
	}

	public void atualizarPontuacao(int p){

		int melhorPontuacao = prefs.getInteger("melhorPontuacao", 0);

		if(melhorPontuacao < p){
			prefs.putInteger("melhorPontuacao", p);
			prefs.flush();
		}
	}
}
